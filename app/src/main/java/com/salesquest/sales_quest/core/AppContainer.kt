package com.salesquest.sales_quest.core

import android.content.Context
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.Operator
import com.salesquest.sales_quest.data.entity.CustomerEntity
import com.salesquest.sales_quest.services.AchievementService
import com.salesquest.sales_quest.services.DailyTaskService
import com.salesquest.sales_quest.services.QuickActionService
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

    var isInitialized: Boolean = false
        private set

    fun init(context: Context) {
        if (isInitialized) return
        db = AppDatabase.build(context)
        xpService = XpService(db)
        dailyTaskService = DailyTaskService(db)
        achievementService = AchievementService(db)
        quickActionService = QuickActionService(db, xpService, dailyTaskService, achievementService)
        isInitialized = true
        AppLogger.info("AppContainer", "服务初始化完成")
    }

    /** 测试专用: 注入内存数据库后重建服务 (跳过 isInitialized 保护) */
    fun initForTest(testDb: AppDatabase) {
        db = testDb
        xpService = XpService(db)
        dailyTaskService = DailyTaskService(db)
        achievementService = AchievementService(db)
        quickActionService = QuickActionService(db, xpService, dailyTaskService, achievementService)
        isInitialized = true
    }

    // ==================== 客户相关 ====================

    /** 生成客户编号 #001, #002, ... */
    suspend fun generateCustomerNumber(): String {
        val count = db.customerDao().getAll().size
        val num = count + 1
        return "#%03d".format(num)
    }

    /** 新增/编辑客户 (所有字段可选), 返回客户 id */
    suspend fun saveCustomer(params: SaveCustomerParams): String {
        val now = System.currentTimeMillis()
        if (params.id != null) {
            val existing = db.customerDao().getById(params.id) ?: throw IllegalStateException("客户不存在")
            val updated = existing.copy(
                name = params.name ?: existing.name,
                phone = params.phone ?: existing.phone,
                operator = params.operator.code,
                selfReportedCost = params.selfReportedCost ?: existing.selfReportedCost,
                actualCost = params.actualCost ?: existing.actualCost,
                packageName = params.packageName ?: existing.packageName,
                traffic = params.traffic ?: existing.traffic,
                minutes = params.minutes ?: existing.minutes,
                broadband = params.broadband,
                subCards = params.subCards,
                camera = params.camera,
                status = params.stage.code,
                salesStage = params.stage.code,
                nextFollowUpAt = params.nextFollowUpAt ?: existing.nextFollowUpAt,
                note = params.note ?: existing.note,
                updatedAt = now
            )
            db.customerDao().updateCustomer(updated)
            return params.id
        } else {
            val name = if (params.name.isNullOrEmpty()) generateCustomerNumber() else params.name!!
            val customer = CustomerEntity(
                id = IdGenerator.gen("c_"),
                name = name,
                phone = params.phone ?: "",
                operator = params.operator.code,
                selfReportedCost = params.selfReportedCost,
                actualCost = params.actualCost,
                packageName = params.packageName,
                traffic = params.traffic,
                minutes = params.minutes,
                broadband = params.broadband,
                subCards = params.subCards,
                camera = params.camera,
                status = params.stage.code,
                salesStage = params.stage.code,
                nextFollowUpAt = params.nextFollowUpAt,
                note = params.note,
                createdAt = now,
                updatedAt = now
            )
            db.customerDao().insertCustomer(customer)
            return customer.id
        }
    }

    suspend fun deleteCustomer(id: String) {
        db.customerDao().deleteCustomer(id)
    }
}

/** 客户保存参数 (所有字段可选) */
data class SaveCustomerParams(
    val id: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val operator: Operator = Operator.UNKNOWN,
    val selfReportedCost: Int? = null,
    val actualCost: Int? = null,
    val packageName: String? = null,
    val traffic: String? = null,
    val minutes: String? = null,
    val broadband: Boolean = false,
    val subCards: Int = 0,
    val camera: Boolean = false,
    val stage: CustomerStage = CustomerStage.NEW,
    val nextFollowUpAt: Long? = null,
    val note: String? = null
)
