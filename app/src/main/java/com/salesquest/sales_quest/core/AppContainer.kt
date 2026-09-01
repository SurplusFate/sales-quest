package com.salesquest.sales_quest.core

import android.content.Context
import androidx.room.withTransaction
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.Operator
import com.salesquest.sales_quest.data.entity.CustomerEntity
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
        dailyTaskService = DailyTaskService(db) { autoBackupManager.markDirty() }
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
        dailyTaskService = DailyTaskService(db)
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

    // ==================== 客户相关 ====================

    /**
     * 生成客户编号 #001, #002, ...
     *
     * 基于历史最大已分配编号 + 1 (不依赖当前客户数量, 删除客户不回退计数器)
     * 使用 Transaction 保证原子性, 防止并发新增生成相同编号
     * 数据恢复/导入后: 取 max(计数器, 数据库最大编号) + 1 防止冲突
     */
    suspend fun generateCustomerNumber(): String {
        return db.withTransaction {
            val counterMax = db.settingDao().getInt(SettingsKeys.MAX_CUSTOMER_NUMBER)
            val dbMax = db.customerDao().getMaxCustomerNumber()
                ?.removePrefix("#")?.toIntOrNull() ?: 0
            val nextNum = maxOf(counterMax, dbMax) + 1
            db.settingDao().setInt(SettingsKeys.MAX_CUSTOMER_NUMBER, nextNum)
            "#%03d".format(nextNum)
        }
    }

    /** 新增/编辑客户 (所有字段可选, null 表示不修改), 返回客户 id */
    suspend fun saveCustomer(params: SaveCustomerParams): String {
        val now = System.currentTimeMillis()
        if (params.id != null) {
            // === 编辑: null 字段保留原值, 非 null 字段使用新值 ===
            val existing = db.customerDao().getById(params.id) ?: throw IllegalStateException("客户不存在")
            val updated = existing.copy(
                name = params.name ?: existing.name,
                phone = params.phone ?: existing.phone,
                operator = params.operator?.code ?: existing.operator,
                selfReportedCost = params.selfReportedCost ?: existing.selfReportedCost,
                actualCost = params.actualCost ?: existing.actualCost,
                packageName = params.packageName ?: existing.packageName,
                traffic = params.traffic ?: existing.traffic,
                minutes = params.minutes ?: existing.minutes,
                broadband = params.broadband ?: existing.broadband,
                subCards = params.subCards ?: existing.subCards,
                camera = params.camera ?: existing.camera,
                status = params.stage?.code ?: existing.status,
                salesStage = params.stage?.code ?: existing.salesStage,
                nextFollowUpAt = params.nextFollowUpAt ?: existing.nextFollowUpAt,
                note = params.note ?: existing.note,
                updatedAt = now
            )
            db.customerDao().updateCustomer(updated)
            markDirtySafe()
            return params.id
        } else {
            // === 新增: null 字段使用默认值 ===
            val name = if (params.name.isNullOrEmpty()) generateCustomerNumber() else params.name!!
            val customer = CustomerEntity(
                id = IdGenerator.gen("c_"),
                name = name,
                phone = params.phone ?: "",
                operator = params.operator?.code ?: Operator.UNKNOWN.code,
                selfReportedCost = params.selfReportedCost,
                actualCost = params.actualCost,
                packageName = params.packageName,
                traffic = params.traffic,
                minutes = params.minutes,
                broadband = params.broadband ?: false,
                subCards = params.subCards ?: 0,
                camera = params.camera ?: false,
                status = params.stage?.code ?: CustomerStage.NEW.code,
                salesStage = params.stage?.code ?: CustomerStage.NEW.code,
                nextFollowUpAt = params.nextFollowUpAt,
                note = params.note,
                customerNumber = if (name.startsWith("#")) name else null,
                createdAt = now,
                updatedAt = now
            )
            db.customerDao().insertCustomer(customer)
            markDirtySafe()
            return customer.id
        }
    }

    suspend fun deleteCustomer(id: String) {
        db.customerDao().deleteCustomer(id)
        markDirtySafe()
    }

    /** 安全标记数据已变化 (测试环境可能未初始化 autoBackupManager) */
    private fun markDirtySafe() {
        if (this::autoBackupManager.isInitialized) {
            autoBackupManager.markDirty()
        }
    }
}

/** 客户保存参数 (所有字段可选, null 表示不修改/使用默认值) */
data class SaveCustomerParams(
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val operator: Operator? = null,
    val selfReportedCost: Int? = null,
    val actualCost: Int? = null,
    val packageName: String? = null,
    val traffic: String? = null,
    val minutes: String? = null,
    val broadband: Boolean? = null,
    val subCards: Int? = null,
    val camera: Boolean? = null,
    val stage: CustomerStage? = null,
    val nextFollowUpAt: Long? = null,
    val note: String? = null
)
