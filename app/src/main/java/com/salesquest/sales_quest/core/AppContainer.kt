package com.salesquest.sales_quest.core

import android.content.Context
import androidx.room.withTransaction
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.services.AchievementService
import com.salesquest.sales_quest.services.AutoBackupManager
import com.salesquest.sales_quest.services.BackupService
import com.salesquest.sales_quest.services.ConfigService
import com.salesquest.sales_quest.services.DailyStatsService
import com.salesquest.sales_quest.services.DailySummaryService
import com.salesquest.sales_quest.services.DailyTaskService
import com.salesquest.sales_quest.services.ExecutionRecordService
import com.salesquest.sales_quest.services.LevelService
import com.salesquest.sales_quest.services.QuickActionService
import com.salesquest.sales_quest.services.WebDavConfigStore
import com.salesquest.sales_quest.services.WebDavService
import com.salesquest.sales_quest.services.WeeklySummaryService
import com.salesquest.sales_quest.services.XpService

/**
 * 应用级服务定位器 (对应 Riverpod 的 ProviderScope + providers)
 */
object AppContainer {

    lateinit var db: AppDatabase
        private set

    lateinit var xpService: XpService
        private set

    lateinit var dailyTaskService: DailyTaskService
        private set

    lateinit var achievementService: AchievementService
        private set

    lateinit var quickActionService: QuickActionService
        private set

    lateinit var dailyStatsService: DailyStatsService
        private set

    lateinit var configService: ConfigService
        private set

    lateinit var levelService: LevelService
        private set

    lateinit var backupService: BackupService
        private set

    lateinit var webDavConfigStore: WebDavConfigStore
        private set

    lateinit var webDavService: WebDavService
        private set

    lateinit var autoBackupManager: AutoBackupManager
        private set

    lateinit var dailySummaryService: DailySummaryService
        private set

    lateinit var weeklySummaryService: WeeklySummaryService
        private set

    lateinit var executionRecordService: ExecutionRecordService
        private set

    var isInitialized: Boolean = false
        private set

    fun init(context: Context) {
        if (isInitialized) return
        db = AppDatabase.build(context)
        xpService = XpService(db)
        dailyTaskService = DailyTaskService(db, xpService) { autoBackupManager.markDirty() }
        achievementService = AchievementService(db)
        quickActionService = QuickActionService(db, xpService, dailyTaskService, achievementService) {
            autoBackupManager.markDirty()
        }
        dailyStatsService = DailyStatsService(db) { autoBackupManager.markDirty() }
        configService = ConfigService(db) { autoBackupManager.markDirty() }
        levelService = LevelService(db)
        backupService = BackupService(db)
        webDavConfigStore = WebDavConfigStore(context)
        webDavService = WebDavService(context, webDavConfigStore, backupService)
        autoBackupManager = AutoBackupManager(webDavService, webDavConfigStore)
        dailySummaryService = DailySummaryService(db) { autoBackupManager.markDirty() }
        weeklySummaryService = WeeklySummaryService(db)
        executionRecordService = ExecutionRecordService(db, quickActionService) { autoBackupManager.markDirty() }
        isInitialized = true
        AppLogger.info("AppContainer", "服务初始化完成")
    }

    /** 测试专用: 注入内存数据库后重建服务 (跳过 isInitialized 保护) */
    fun initForTest(testDb: AppDatabase) {
        db = testDb
        xpService = XpService(db)
        dailyTaskService = DailyTaskService(db, xpService)
        achievementService = AchievementService(db)
        quickActionService = QuickActionService(db, xpService, dailyTaskService, achievementService) {
            autoBackupManager.markDirty()
        }
        dailyStatsService = DailyStatsService(db)
        configService = ConfigService(db)
        levelService = LevelService(db)
        backupService = BackupService(db)
        dailySummaryService = DailySummaryService(db)
        weeklySummaryService = WeeklySummaryService(db)
        executionRecordService = ExecutionRecordService(db, quickActionService)
        isInitialized = true
    }

    /** 清除全部数据 (设置页-清空所有), 并同步清除 pending 备份标记, 防止旧备份覆盖恢复数据 (S2-7) */
    suspend fun clearAllData() {
        db.clearAllData()
        if (::webDavConfigStore.isInitialized) {
            webDavConfigStore.setPendingBackup(false)
        }
    }

    /** 安全标记数据已变化 (测试环境可能未初始化 autoBackupManager) */
    private fun markDirtySafe() {
        if (this::autoBackupManager.isInitialized) {
            autoBackupManager.markDirty()
        }
    }
}
