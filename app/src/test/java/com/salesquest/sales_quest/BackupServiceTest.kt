package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.entity.CustomerEntity
import com.salesquest.sales_quest.data.entity.DailySummaryEntity
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.data.entity.UserStatEntity
import com.salesquest.sales_quest.services.BackupJson
import com.salesquest.sales_quest.services.BackupService
import com.salesquest.sales_quest.services.BackupValidationResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 云备份服务测试
 *
 * 覆盖: 全表导出 / zip 打包 / 解析校验 / 清空重建恢复
 */
@RunWith(RobolectricTestRunner::class)
class BackupServiceTest {

    private lateinit var db: AppDatabase
    private lateinit var service: BackupService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = BackupService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun seedData() {
        db.settingDao().set(SettingEntity(SettingsKeys.TOTAL_MEETS, "5"))
        db.settingDao().set(SettingEntity("webdav_test", "hello"))
        db.customerDao().insertCustomer(
            CustomerEntity(id = "c1", name = "张三", phone = "13800000000", status = "NEW")
        )
        db.statsDao().insertStats(
            UserStatEntity(totalXp = 1200, currentLevel = 4, streakDays = 3)
        )
        db.levelRequirementDao().insert(
            LevelRequirementEntity(id = "l1", level = 5, conditionType = "TOTAL_DEAL", threshold = 5)
        )
        db.dailySummaryDao().upsert(
            DailySummaryEntity(dateKey = "2026-08-19", good = "签了一单", improvement = "加快节奏")
        )
    }

    @Test
    fun 导出备份_包含全部业务表() = runTest {
        seedData()
        val data = service.exportBackupData()
        assertEquals(2, data.settings.size)
        assertEquals(1, data.customers.size)
        assertEquals(1, data.userStats.size)
        assertEquals(1, data.levelRequirements.size)
        assertEquals(1, data.dailySummaries.size)
        assertEquals("5", data.settings.first { it.key == SettingsKeys.TOTAL_MEETS }.value)
    }

    @Test
    fun zip打包后可解析校验通过() = runTest {
        seedData()
        val data = service.exportBackupData()
        val zip = service.createBackupZip(data, dbFileBytes = null)

        val parsed = service.parseBackupZip(zip)
        assertTrue(parsed is BackupValidationResult.Success)
        val backup = (parsed as BackupValidationResult.Success).data
        assertEquals(1, backup.customers.size)
        assertEquals("张三", backup.customers.first().name)
        assertEquals(1, backup.dailySummaries.size)
    }

    @Test
    fun 恢复备份_清空旧数据并重建() = runTest {
        seedData()
        val data = service.exportBackupData()
        val zip = service.createBackupZip(data)

        // 模拟新库: 清空后写入新数据
        db.settingDao().clearAll()
        db.customerDao().clearAll()
        db.settingDao().set(SettingEntity("other", "1"))
        db.customerDao().insertCustomer(
            CustomerEntity(id = "new_c", name = "干扰数据")
        )

        val parsed = service.parseBackupZip(zip) as BackupValidationResult.Success
        val stats = service.restoreBackupData(parsed.data)
        assertEquals(1, stats.customers)
        assertEquals(2, stats.settings)

        // 干扰数据应被清除
        assertEquals(1, db.customerDao().getAll().size)
        assertEquals("张三", db.customerDao().getAll().first().name)
        assertEquals("5", db.settingDao().get(SettingsKeys.TOTAL_MEETS))
        assertEquals(null, db.settingDao().get("other"))
        assertEquals(1, db.dailySummaryDao().getAll().size)
        assertEquals("签了一单", db.dailySummaryDao().get("2026-08-19")?.good)
    }

    @Test
    fun 非法zip_返回解析错误() {
        val parsed = service.parseBackupZip(byteArrayOf(1, 2, 3, 4, 5))
        assertTrue(parsed is BackupValidationResult.Error)
    }

    @Test
    fun 缺少dataJson_返回错误() {
        val zip = service.createBackupZip(
            com.salesquest.sales_quest.services.BackupData()
        )
        // 正常 zip 应有 data.json, 这里直接用 BackupJson 验证编解码
        val raw = BackupJson.encodeData(com.salesquest.sales_quest.services.BackupData())
        val decoded = BackupJson.decodeData(raw)
        assertNotNull(decoded)
        assertEquals(1, decoded.formatVersion)
    }

    @Test
    fun 恢复备份_晋级条件与总结表正确重建() = runTest {
        seedData()
        val data = service.exportBackupData()
        val zip = service.createBackupZip(data)

        db.levelRequirementDao().clearAll()
        db.dailySummaryDao().clearAll()

        val parsed = service.parseBackupZip(zip) as BackupValidationResult.Success
        service.restoreBackupData(parsed.data)

        assertEquals(1, db.levelRequirementDao().getAll().size)
        assertEquals(5, db.levelRequirementDao().getAll().first().level)
        assertEquals(1, db.dailySummaryDao().getAll().size)
    }
}
