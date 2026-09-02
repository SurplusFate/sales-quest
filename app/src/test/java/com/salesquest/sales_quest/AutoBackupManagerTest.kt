package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.AutoBackupManager
import com.salesquest.sales_quest.services.BackupService
import com.salesquest.sales_quest.services.WebDavConfig
import com.salesquest.sales_quest.services.WebDavConfigStore
import com.salesquest.sales_quest.services.WebDavResult
import com.salesquest.sales_quest.services.WebDavService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 自动备份管理器测试
 *
 * 覆盖 7 个场景:
 * 1. 没有数据变化 → 不执行备份
 * 2. 产生一次数据变化 → 触发备份
 * 3. 短时间连续产生数据 → 只执行 1 次备份
 * 4. 备份失败 → dirty 保持 true
 * 5. 备份过程中产生新数据 → 继续安排下一次备份
 * 6. 自动备份关闭 → 不上传
 * 7. WebDAV 未配置 → 不影响业务
 */
@RunWith(RobolectricTestRunner::class)
class AutoBackupManagerTest {

    private lateinit var db: AppDatabase
    private lateinit var configStore: WebDavConfigStore
    private lateinit var backupService: BackupService
    private lateinit var fakeWebDavService: FakeWebDavService
    private lateinit var manager: AutoBackupManager
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backupService = BackupService(db)
        configStore = WebDavConfigStore(context)
        fakeWebDavService = FakeWebDavService(context, configStore, backupService)
        manager = AutoBackupManager(
            webDavService = fakeWebDavService,
            configStore = configStore,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            delayMs = 0L  // 测试中不延迟, 立即触发
        )
        manager.resetForTest()

        // 默认配置: 开启自动备份 + WebDAV 已配置
        configStore.save(WebDavConfig(
            url = "https://dav.jianguoyun.com/dav",
            username = "user",
            password = "pass",
            autoBackup = true
        ))
    }

    @After
    fun tearDown() {
        manager.shutdown()
        db.close()
    }

    // ==================== 测试 1: 没有数据变化 → 不备份 ====================

    @Test
    fun 没有数据变化时不执行自动备份() = runBlocking {
        manager.triggerBackupNowForTest()

        assertEquals(0, manager.backupExecutionCount)
        assertTrue(manager.lastBackupResult is AutoBackupManager.BackupOutcome.Skipped)
    }

    // ==================== 启动补偿: pending 持久化标记 ====================

    @Test
    fun 启动补偿_pending标记存在时重新触发备份() = runBlocking {
        // 模拟上次进程退出前留下未完成的备份标记
        configStore.setPendingBackup(true)

        manager.resumeIfPending()

        assertTrue(manager.isDirty())
        assertEquals(1, manager.getDataChangeVersion())

        // 备份成功后 pending 标记被清除
        manager.triggerBackupNowForTest()

        assertFalse(configStore.isPendingBackup())
        assertFalse(manager.isDirty())
        assertEquals(1, manager.backupExecutionCount)
    }

    @Test
    fun 启动补偿_无pending标记时不做任何事() = runBlocking {
        manager.resumeIfPending()

        assertFalse(manager.isDirty())
        assertEquals(0, manager.getDataChangeVersion())
        assertEquals(0, manager.backupExecutionCount)
    }


    // ==================== 测试 2: 产生一次数据变化 → 触发备份 ====================

    @Test
    fun 产生一次数据变化后触发备份() = runBlocking {
        manager.markDirty()

        assertTrue(manager.isDirty())
        assertEquals(1, manager.getDataChangeVersion())

        manager.triggerBackupNowForTest()

        assertEquals(1, manager.backupExecutionCount)
        assertTrue(manager.lastBackupResult is AutoBackupManager.BackupOutcome.Success)
        assertFalse(manager.isDirty())
    }

    // ==================== 测试 3: 连续数据变化 → 只备份 1 次 ====================

    @Test
    fun 短时间连续数据变化只产生一次备份() = runBlocking {
        // 模拟连续 4 次数据变化
        manager.markDirty()
        manager.markDirty()
        manager.markDirty()
        manager.markDirty()

        assertEquals(4, manager.getDataChangeVersion())
        assertTrue(manager.isDirty())

        // 延迟到期后执行备份
        manager.triggerBackupNowForTest()

        assertEquals(1, manager.backupExecutionCount)
        assertTrue(manager.lastBackupResult is AutoBackupManager.BackupOutcome.Success)
        assertFalse(manager.isDirty())
    }

    // ==================== 测试 4: 备份失败 → dirty 保持 true ====================

    @Test
    fun 备份失败时dirty保持true() = runBlocking {
        fakeWebDavService.backupResult = WebDavResult.Failure("网络错误")

        manager.markDirty()
        manager.triggerBackupNowForTest()

        assertEquals(1, manager.backupExecutionCount)
        assertTrue(manager.lastBackupResult is AutoBackupManager.BackupOutcome.Failure)
        assertTrue("dirty 应保持 true", manager.isDirty())
    }

    // ==================== 测试 5: 备份过程中产生新数据 → 继续安排备份 ====================

    @Test
    fun 备份过程中产生新数据时继续安排下一次备份() = runBlocking {
        // 模拟: markDirty (version=1) → 备份开始 → 备份期间 markDirty (version=2) → 备份成功但版本不匹配
        manager.markDirty()
        val versionBeforeBackup = manager.getDataChangeVersion()
        assertEquals(1, versionBeforeBackup)

        // 模拟备份过程中产生新数据: backupNow 执行时触发 markDirty
        fakeWebDavService.onBackupStart = { manager.markDirty() }

        // 执行备份 (此时版本号已变化)
        manager.triggerBackupNowForTest()

        assertEquals(1, manager.backupExecutionCount)
        // dirty 应保持 true (因为备份期间产生了新数据, 版本号不匹配)
        assertTrue("备份期间有新数据, dirty 应保持 true", manager.isDirty())
        assertTrue(manager.getDataChangeVersion() > versionBeforeBackup)
    }

    // ==================== 测试 6: 自动备份关闭 → 不上传 ====================

    @Test
    fun 自动备份关闭时不执行备份() = runBlocking {
        configStore.setAutoBackup(false)
        configStore.save(WebDavConfig(
            url = "https://dav.jianguoyun.com/dav",
            username = "user",
            password = "pass",
            autoBackup = false
        ))

        manager.markDirty()
        manager.triggerBackupNowForTest()

        assertEquals(0, manager.backupExecutionCount)
        assertTrue(manager.lastBackupResult is AutoBackupManager.BackupOutcome.Skipped)
    }

    // ==================== 测试 7: WebDAV 未配置 → 不影响业务 ====================

    @Test
    fun webDAV未配置时不影响业务() = runBlocking {
        // 清空配置 (模拟未配置)
        configStore.clear()

        // markDirty 应该不触发任何异常
        manager.markDirty()

        // 不产生备份
        manager.triggerBackupNowForTest()

        assertEquals(0, manager.backupExecutionCount)
        assertTrue(manager.lastBackupResult is AutoBackupManager.BackupOutcome.Skipped)
    }

    /**
     * Fake WebDavService: 继承 WebDavService 但覆盖 backupNow()
     * 只记录调用, 不真正执行网络请求
     * onBackupStart: 备份开始时的回调 (用于模拟备份过程中产生新数据)
     */
    private class FakeWebDavService(
        context: Context,
        configStore: WebDavConfigStore,
        backupService: BackupService
    ) : WebDavService(context, configStore, backupService) {
        var backupResult: WebDavResult = WebDavResult.Success("测试备份成功")
        var onBackupStart: (() -> Unit)? = null

        override suspend fun backupNow(config: WebDavConfig): WebDavResult {
            onBackupStart?.invoke()
            return backupResult
        }
    }
}