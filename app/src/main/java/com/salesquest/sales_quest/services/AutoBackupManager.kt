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
import java.util.concurrent.atomic.AtomicReference

/**
 * 自动备份管理器 — 数据变化触发 + 防抖延迟 + 版本号并发控制
 *
 * 核心机制:
 * 1. 数据变化 → markDirty() → 版本号++ → 取消旧延迟任务 → 启动新延迟任务 (2 分钟)
 * 2. 延迟到期 → 检查条件 → 记录备份版本 → 上传 → 比较版本 → 决定是否标记完成
 * 3. 失败时保持 dirty=true, 下次数据变化时重新触发
 *
 * 线程安全: markDirty() 可从任意线程调用, delayJob 使用 AtomicReference 保证原子替换
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

        /** 失败重试最大退避时间 (30 分钟) */
        const val MAX_BACKOFF_MS = 30 * 60 * 1000L
    }

    /** 是否有未备份的数据变化 */
    private val dirty = AtomicBoolean(false)

    /** 数据变化版本号 (每次 markDirty 递增) */
    private val dataChangeVersion = AtomicInteger(0)

    /** 连续失败次数 (用于指数退避) */
    private val retryCount = AtomicInteger(0)

    /** 当前延迟任务 (AtomicReference 保证并发 markDirty 时不会丢失任务引用) */
    private val delayJobRef = AtomicReference<Job?>(null)

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
     *
     * delayMs > 0 (生产): 启动延迟备份协程
     * delayMs <= 0 (测试): 仅更新状态, 不启动后台协程 (由 triggerBackupNowForTest 控制)
     */
    fun markDirty() {
        try {
            val config = configStore.load()
            if (!config.autoBackup) return
            if (!config.isConfigured()) return
        } catch (e: Exception) {
            AppLogger.error("AutoBackupManager", "读取备份配置失败: ${e.message}")
            return
        }

        dirty.set(true)
        configStore.setPendingBackup(true)
        val version = dataChangeVersion.incrementAndGet()
        AppLogger.info("AutoBackupManager", "markDirty: version=$version, dirty=true")

        // 取消旧的延迟任务, 启动新的 (原子替换, 保证并发安全)
        if (delayMs > 0) {
            val newJob = scope.launch {
                delay(delayMs)
                executeBackup()
            }
            val oldJob = delayJobRef.getAndSet(newJob)
            oldJob?.cancel()
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
            if (!config.isConfigured()) {
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
                    retryCount.set(0)
                    configStore.setPendingBackup(false)
                    lastBackupResult = BackupOutcome.Success
                    AppLogger.info("AutoBackupManager", "备份成功, dirty=false (version=$currentVersion)")
                } else {
                    // 备份期间产生了新数据, 继续安排下一次备份
                    retryCount.set(0)
                    AppLogger.info("AutoBackupManager", "备份成功但版本不匹配 (backup=$backupVersion, current=$currentVersion), 继续延迟备份")
                    scheduleNextBackup()
                }
            } else {
                // 上传失败, 保持 dirty=true 并按指数退避重试 (进程存活期间不依赖下次 markDirty)
                val msg = (result as? WebDavResult.Failure)?.message ?: "unknown"
                lastBackupResult = BackupOutcome.Failure(msg)
                AppLogger.error("AutoBackupManager", "备份失败, dirty保持true: $msg")
                retryCount.incrementAndGet()
                scheduleNextBackup()
            }
        } catch (e: Exception) {
            lastBackupResult = BackupOutcome.Failure(e.message ?: e.javaClass.simpleName)
            AppLogger.error("AutoBackupManager", "备份异常: ${e.message}", e.stackTraceToString())
            // dirty 保持 true, 按指数退避重试
            retryCount.incrementAndGet()
            scheduleNextBackup()
        } finally {
            backupMutex.unlock()
        }
    }

    /** 重新安排延迟备份 (失败重试 / 备份期间产生新数据时), 失败重试按指数退避 */
    private fun scheduleNextBackup() {
        if (delayMs <= 0) return
        val backoffMs = minOf(delayMs * (1L shl minOf(retryCount.get(), 4)), MAX_BACKOFF_MS)
        val newJob = scope.launch {
            delay(backoffMs)
            executeBackup()
        }
        val oldJob = delayJobRef.getAndSet(newJob)
        oldJob?.cancel()
    }

    /**
     * 启动补偿: 上次进程退出前存在未完成的备份 (pending 标记) 时重新触发。
     * 覆盖"录完数据 → 进程被系统回收 → 延迟协程消失 → 备份永不发生"的丢失路径。
     */
    fun resumeIfPending() {
        try {
            if (configStore.isPendingBackup()) {
                AppLogger.info("AutoBackupManager", "启动补偿: 发现未完成的备份, 重新触发")
                markDirty()
            }
        } catch (e: Exception) {
            AppLogger.error("AutoBackupManager", "启动补偿读取 pending 标记失败: ${e.message}")
        }
    }

    /** 仅供测试: 直接触发备份 (跳过延迟, 取消挂起的后台任务) */
    suspend fun triggerBackupNowForTest() {
        // 取消 markDirty 启动的延迟备份, 避免与测试直接触发的备份竞争
        val oldJob = delayJobRef.getAndSet(null)
        oldJob?.cancel()
        executeBackup()
        // 取消 executeBackup 中 scheduleNextBackup 可能启动的后台任务
        val pendingJob = delayJobRef.getAndSet(null)
        pendingJob?.cancel()
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
        retryCount.set(0)
        val oldJob = delayJobRef.getAndSet(null)
        oldJob?.cancel()
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
        val oldJob = delayJobRef.getAndSet(null)
        oldJob?.cancel()
        scope.cancel()
    }
}
