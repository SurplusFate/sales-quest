package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 自动备份管理器 — 数据变化触发 + 防抖延迟 + 版本号并发控制
 *
 * 核心机制:
 * 1. 数据变化 → markDirty() → 版本号++ → 取消旧延迟任务 → 启动新延迟任务 (2 分钟)
 * 2. 延迟到期 → 检查条件 → 记录备份版本 → 上传 → 比较版本 → 决定是否标记完成
 * 3. 失败时保持 dirty=true, 下次数据变化时重新触发
 *
 * 线程安全: markDirty() 可从任意线程调用, 内部用 Mutex 保证备份执行串行化
 */
class AutoBackupManager(
    private val webDavService: WebDavService,
    private val configStore: WebDavConfigStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val delayMs: Long = DEFAULT_DELAY_MS
) {

    companion object {
        /** 防抖延迟时间 (2 分钟) */
        const val DEFAULT_DELAY_MS = 2 * 60 * 1000L
    }

    /** 是否有未备份的数据变化 */
    private val dirty = AtomicBoolean(false)

    /** 数据变化版本号 (每次 markDirty 递增) */
    private val dataChangeVersion = AtomicInteger(0)

    /** 当前延迟任务 */
    private var delayJob: Job? = null

    /** 备份执行锁 (保证同一时间只有一个备份在执行) */
    private val backupMutex = Mutex()

    /** 用于测试: 备份执行次数 */
    @Volatile
    var backupExecutionCount: Int = 0
        private set

    /** 用于测试: 上次备份结果 */
    @Volatile
    var lastBackupResult: BackupOutcome? = null
        private set

    sealed class BackupOutcome {
        object Skipped : BackupOutcome()
        object Success : BackupOutcome()
        data class Failure(val reason: String) : BackupOutcome()
    }

    /**
     * 标记数据已变化, 触发防抖延迟备份
     *
     * 线程安全, 可从任意线程调用
     */
    fun markDirty() {
        if (!configStore.load().autoBackup) return
        if (!configStore.isConfigured()) return

        dirty.set(true)
        val version = dataChangeVersion.incrementAndGet()
        AppLogger.info("AutoBackupManager", "markDirty: version=$version, dirty=true")

        // 取消旧的延迟任务, 启动新的
        delayJob?.cancel()
        delayJob = scope.launch {
            delay(delayMs)
            executeBackup()
        }
    }

    /**
     * 执行自动备份 (延迟到期后调用)
     *
     * 条件检查 → 版本记录 → 上传 → 版本比较
     */
    private suspend fun executeBackup() {
        if (!backupMutex.tryLock()) {
            AppLogger.info("AutoBackupManager", "已有备份在执行, 跳过")
            return
        }

        try {
            // 前置条件检查
            if (!dirty.get()) {
                lastBackupResult = BackupOutcome.Skipped
                return
            }

            val config = configStore.load()
            if (!config.autoBackup) {
                dirty.set(false)
                lastBackupResult = BackupOutcome.Skipped
                return
            }
            if (!configStore.isConfigured()) {
                lastBackupResult = BackupOutcome.Skipped
                return
            }

            // 记录备份开始时的数据版本
            val backupVersion = dataChangeVersion.get()
            AppLogger.info("AutoBackupManager", "开始备份: backupVersion=$backupVersion")

            backupExecutionCount++

            val result = webDavService.backupNow(config)

            if (result is WebDavResult.Success) {
                // 上传成功, 检查版本号
                val currentVersion = dataChangeVersion.get()
                if (currentVersion == backupVersion) {
                    // 备份期间无新数据, 标记完成
                    dirty.set(false)
                    lastBackupResult = BackupOutcome.Success
                    AppLogger.info("AutoBackupManager", "备份成功, dirty=false (version=$currentVersion)")
                } else {
                    // 备份期间产生了新数据, 继续安排下一次备份
                    AppLogger.info("AutoBackupManager", "备份成功但版本不匹配 (backup=$backupVersion, current=$currentVersion), 继续延迟备份")
                    scheduleNextBackup()
                }
            } else {
                // 上传失败, 保持 dirty=true
                val msg = (result as? WebDavResult.Failure)?.message ?: "unknown"
                lastBackupResult = BackupOutcome.Failure(msg)
                AppLogger.error("AutoBackupManager", "备份失败, dirty保持true: $msg")
                // dirty 保持 true, 下次 markDirty 时会重新触发
            }
        } catch (e: Exception) {
            lastBackupResult = BackupOutcome.Failure(e.message ?: e.javaClass.simpleName)
            AppLogger.error("AutoBackupManager", "备份异常: ${e.message}")
            // dirty 保持 true
        } finally {
            backupMutex.unlock()
        }
    }

    /** 重新安排延迟备份 (备份期间产生新数据时) */
    private fun scheduleNextBackup() {
        delayJob?.cancel()
        delayJob = scope.launch {
            delay(delayMs)
            executeBackup()
        }
    }

    /** 仅供测试: 直接触发备份 (跳过延迟) */
    suspend fun triggerBackupNowForTest() {
        executeBackup()
    }

    /** 仅供测试: 获取当前 dirty 状态 */
    fun isDirtyForTest(): Boolean = dirty.get()

    /** 仅供测试: 获取当前版本号 */
    fun getDataChangeVersionForTest(): Int = dataChangeVersion.get()

    /** 仅供测试: 获取 dirty 的当前值 (用于断言) */
    fun isDirty(): Boolean = dirty.get()

    /** 仅供测试: 获取版本号 */
    fun getDataChangeVersion(): Int = dataChangeVersion.get()

    /** 仅供测试: 重置状态 */
    fun resetForTest() {
        dirty.set(false)
        dataChangeVersion.set(0)
        delayJob?.cancel()
        delayJob = null
        backupExecutionCount = 0
        lastBackupResult = null
    }

    /** 仅供测试: 设置延迟时间 */
    var testDelayMs: Long = delayMs

    /** 仅供测试: 替换 delay 时间 */
    fun setDelayMsForTest(ms: Long) {
        testDelayMs = ms
    }

    /** 释放资源 */
    fun shutdown() {
        delayJob?.cancel()
        scope.cancel()
    }
}
