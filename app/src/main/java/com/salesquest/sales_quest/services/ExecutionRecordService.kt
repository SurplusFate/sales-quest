package com.salesquest.sales_quest.services

import androidx.room.withTransaction
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.entity.ExecutionRecordEntity

/**
 * 执行记录服务 — 分段执行记录的增删改查 + 每日累计自动重算
 *
 * 核心规则:
 * 1. 每条记录只保存 "新增量" (delta), 不是当天累计
 * 2. 当天累计 = 该日期所有记录之和, 自动写入 settings (people_seen_/queries_/deals_)
 * 3. 第一次为某日期添加记录时, 若 settings 已有数据, 自动创建一条 DAILY_TOTAL 基准记录
 *    保证旧数据平滑过渡: 旧总量 + 新增量 = 新总量
 * 4. 修改/删除记录后自动重算当天累计和累计总计
 * 5. 今天的数据变化会触发 QuickActionService.refreshAfterDataChange() (任务/XP/成就)
 * 6. 所有操作使用 Transaction 保证原子性
 */
class ExecutionRecordService(
    private val db: AppDatabase,
    private val quickActionService: QuickActionService? = null,
    private val onDataChanged: () -> Unit = {}
) {

    companion object {
        const val PRECISION_EXACT = "EXACT"
        const val PRECISION_PERIOD = "PERIOD"
        const val PRECISION_DAILY_TOTAL = "DAILY_TOTAL"
    }

    /**
     * 添加一条执行记录
     *
     * @param dateKey 日期 yyyy-MM-dd
     * @param recordTime 记录时间戳 (EXACT/PERIOD 使用, DAILY_TOTAL 传 null)
     * @param timePrecision EXACT / PERIOD / DAILY_TOTAL
     * @param periodLabel 时段标签 (PERIOD 使用, 如 "上午"; 其他传 null)
     * @param peopleSeen 新增见人数
     * @param queries 新增查询数
     * @param deals 新增成交数
     */
    suspend fun addRecord(
        dateKey: String,
        recordTime: Long?,
        timePrecision: String,
        periodLabel: String?,
        peopleSeen: Int,
        queries: Int,
        deals: Int
    ): String {
        // 基本校验 + 单条记录漏斗校验 (成交 <= 查询 <= 见人)
        FunnelValidator.validate(peopleSeen, queries, deals)

        val recordId = IdGenerator.gen("er_")
        val now = System.currentTimeMillis()

        db.withTransaction {
            // 首次添加: 若 settings 已有该日期数据, 自动创建基准记录
            ensureBaseRecordIfNeeded(dateKey)

            // 插入新记录
            val record = ExecutionRecordEntity(
                id = recordId,
                dateKey = dateKey,
                recordTime = recordTime,
                timePrecision = timePrecision,
                periodLabel = periodLabel,
                peopleSeen = peopleSeen,
                queries = queries,
                deals = deals,
                createdAt = now,
                updatedAt = now
            )
            db.executionRecordDao().insert(record)

            // 重算当天累计
            recalculateDailyTotal(dateKey)
        }

        // 今天: 触发任务/XP/成就
        if (dateKey == DateUtil.dateKey() && quickActionService != null) {
            try {
                quickActionService.refreshAfterDataChange()
            } catch (e: Exception) {
                AppLogger.error("ExecutionRecordService", "触发任务/XP刷新失败: ${e.message}", e.stackTraceToString())
            }
        }

        onDataChanged()
        return recordId
    }

    /**
     * 修改一条执行记录
     */
    suspend fun updateRecord(
        id: String,
        peopleSeen: Int,
        queries: Int,
        deals: Int
    ) {
        // 基本校验 + 单条记录漏斗校验 (成交 <= 查询 <= 见人)
        FunnelValidator.validate(peopleSeen, queries, deals)

        val dateKey = db.withTransaction {
            val existing = db.executionRecordDao().getById(id)
                ?: throw IllegalArgumentException("记录不存在")

            val updated = existing.copy(
                peopleSeen = peopleSeen,
                queries = queries,
                deals = deals,
                updatedAt = System.currentTimeMillis()
            )
            db.executionRecordDao().update(updated)

            // 重算当天累计
            recalculateDailyTotal(existing.dateKey)
            existing.dateKey
        }

        // 今天: 触发任务/XP/成就
        if (dateKey == DateUtil.dateKey() && quickActionService != null) {
            try {
                quickActionService.refreshAfterDataChange()
            } catch (e: Exception) {
                AppLogger.error("ExecutionRecordService", "触发任务/XP刷新失败: ${e.message}", e.stackTraceToString())
            }
        }

        onDataChanged()
    }

    /**
     * 删除一条执行记录
     */
    suspend fun deleteRecord(id: String) {
        val dateKey = db.withTransaction {
            val existing = db.executionRecordDao().getById(id)
                ?: throw IllegalArgumentException("记录不存在")

            db.executionRecordDao().delete(id)

            // 重算当天累计
            recalculateDailyTotal(existing.dateKey)
            existing.dateKey
        }

        // 今天: 触发任务/XP/成就
        if (dateKey == DateUtil.dateKey() && quickActionService != null) {
            try {
                quickActionService.refreshAfterDataChange()
            } catch (e: Exception) {
                AppLogger.error("ExecutionRecordService", "触发任务/XP刷新失败: ${e.message}", e.stackTraceToString())
            }
        }

        onDataChanged()
    }

    /** 获取某天的所有执行记录 */
    suspend fun getRecords(dateKey: String): List<ExecutionRecordEntity> {
        return db.executionRecordDao().getByDate(dateKey)
    }

    /** 监听某天的执行记录变化 (用于 UI 响应式更新) */
    fun watchRecords(dateKey: String) = db.executionRecordDao().watchByDate(dateKey)

    /** 获取有执行记录的所有日期 (用于历史列表) */
    suspend fun getAllDates(): List<String> {
        return db.executionRecordDao().getAllDates()
    }

    // ==================== 累计值录入 (需求1) ====================

    /**
     * 某日"当前累计"基准
     *
     * 规则: 当天已有执行记录时以"记录之和"为准, 否则回退到 settings 中的旧数据。
     * 这样既保证 累计 = 记录之和 的不变量, 又兼容历史数据 (旧总量来自 settings)。
     */
    data class DailyCumulative(
        val peopleSeen: Int,
        val queries: Int,
        val deals: Int,
        val fromRecords: Boolean
    )

    /** 累计值差值 (最新累计 - 当前累计) */
    data class CumulativeDelta(
        val peopleSeen: Int,
        val queries: Int,
        val deals: Int
    ) {
        val isZero: Boolean get() = peopleSeen == 0 && queries == 0 && deals == 0
        val hasNegative: Boolean get() = peopleSeen < 0 || queries < 0 || deals < 0
    }

    /** 累计值录入结果 */
    sealed class CumulativeApplyResult {
        /** 数据未变化, 未写库 */
        object NoChange : CumulativeApplyResult()

        /** 已写入, 返回新记录 id 与本次差值 */
        data class Saved(val recordId: String, val delta: CumulativeDelta) : CumulativeApplyResult()

        /** 差值为负, 需要用户确认后才写入 */
        data class NeedConfirm(val delta: CumulativeDelta) : CumulativeApplyResult()
    }

    /** 读取某日当前累计基准 */
    suspend fun getDailyCumulative(dateKey: String): DailyCumulative {
        val records = db.executionRecordDao().getByDate(dateKey)
        if (records.isEmpty()) {
            return DailyCumulative(
                peopleSeen = db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)),
                queries = db.settingDao().getInt(SettingsKeys.queries(dateKey)),
                deals = db.settingDao().getInt(SettingsKeys.deals(dateKey)),
                fromRecords = false
            )
        }
        return DailyCumulative(
            peopleSeen = records.sumOf { it.peopleSeen },
            queries = records.sumOf { it.queries },
            deals = records.sumOf { it.deals },
            fromRecords = true
        )
    }

    /** 纯计算差值 (不写库), 供 UI 实时预览与负值判定 */
    suspend fun previewDelta(
        dateKey: String,
        latestPeopleSeen: Int,
        latestQueries: Int,
        latestDeals: Int
    ): CumulativeDelta {
        val base = getDailyCumulative(dateKey)
        return CumulativeDelta(
            peopleSeen = latestPeopleSeen - base.peopleSeen,
            queries = latestQueries - base.queries,
            deals = latestDeals - base.deals
        )
    }

    /**
     * 累计值录入: 用户填写"最新累计值", App 自动算差值并落一条执行记录
     *
     * 1. 最新累计值本身必须满足漏斗约束 (0 <= 成交 <= 查询 <= 见人), 否则抛 IllegalArgumentException
     * 2. 差值为 0 → NoChange, 不写库
     * 3. 差值为负且未确认 → NeedConfirm, 不写库; 确认后 allowNegative=true 再调用
     * 4. 写入后按"记录之和"重算当天累计 (事务内), 因此 当天累计 == 用户填写的最新累计值
     * 5. 今天的数据变化触发任务/XP/成就刷新, 并标记自动备份 dirty
     */
    suspend fun applyCumulativeInput(
        dateKey: String,
        recordTime: Long?,
        timePrecision: String,
        periodLabel: String?,
        latestPeopleSeen: Int,
        latestQueries: Int,
        latestDeals: Int,
        allowNegative: Boolean = false
    ): CumulativeApplyResult {
        // 最新累计值必须合法
        FunnelValidator.validate(latestPeopleSeen, latestQueries, latestDeals)

        val base = getDailyCumulative(dateKey)
        val delta = CumulativeDelta(
            peopleSeen = latestPeopleSeen - base.peopleSeen,
            queries = latestQueries - base.queries,
            deals = latestDeals - base.deals
        )

        if (delta.isZero) return CumulativeApplyResult.NoChange
        if (delta.hasNegative && !allowNegative) return CumulativeApplyResult.NeedConfirm(delta)

        val recordId = IdGenerator.gen("er_")
        val now = System.currentTimeMillis()

        db.withTransaction {
            // 历史数据平滑过渡: 首次为某日期建档时, 用 settings 旧值创建基准记录
            ensureBaseRecordIfNeeded(dateKey)

            // 累积差值可能为负 (下调数据), 单条记录不做漏斗校验,
            // 改由 recalculateDailyTotal 对"重算后的当天总量"做漏斗校验
            db.executionRecordDao().insert(
                ExecutionRecordEntity(
                    id = recordId,
                    dateKey = dateKey,
                    recordTime = recordTime,
                    timePrecision = timePrecision,
                    periodLabel = periodLabel,
                    peopleSeen = delta.peopleSeen,
                    queries = delta.queries,
                    deals = delta.deals,
                    createdAt = now,
                    updatedAt = now
                )
            )

            recalculateDailyTotal(dateKey)
        }

        // 今天: 触发任务/XP/成就
        if (dateKey == DateUtil.dateKey() && quickActionService != null) {
            try {
                quickActionService.refreshAfterDataChange()
            } catch (e: Exception) {
                AppLogger.error("ExecutionRecordService", "触发任务/XP刷新失败: ${e.message}", e.stackTraceToString())
            }
        }

        onDataChanged()
        return CumulativeApplyResult.Saved(recordId, delta)
    }

    /**
     * 首次为某日期添加记录时, 若 settings 已有数据, 自动创建 DAILY_TOTAL 基准记录
     *
     * 这样保证: 旧总量 + 新增量 = 新总量
     * 基准记录可以像普通记录一样被编辑或删除
     */
    private suspend fun ensureBaseRecordIfNeeded(dateKey: String) {
        if (db.executionRecordDao().countByDate(dateKey) > 0) return

        val existingPeople = db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey))
        val existingQueries = db.settingDao().getInt(SettingsKeys.queries(dateKey))
        val existingDeals = db.settingDao().getInt(SettingsKeys.deals(dateKey))

        if (existingPeople > 0 || existingQueries > 0 || existingDeals > 0) {
            val now = System.currentTimeMillis()
            val baseRecord = ExecutionRecordEntity(
                id = IdGenerator.gen("er_"),
                dateKey = dateKey,
                recordTime = null,
                timePrecision = PRECISION_DAILY_TOTAL,
                periodLabel = null,
                peopleSeen = existingPeople,
                queries = existingQueries,
                deals = existingDeals,
                createdAt = now,
                updatedAt = now
            )
            db.executionRecordDao().insert(baseRecord)
            AppLogger.info("ExecutionRecordService", "自动创建基准记录: $dateKey (people=$existingPeople, queries=$existingQueries, deals=$existingDeals)")
        }
    }

    /**
     * 重算某天的累计数据并写入 settings
     *
     * 累计 = 该日期所有执行记录之和
     * 同时重算总累计 (所有日期之和)
     */
    private suspend fun recalculateDailyTotal(dateKey: String) {
        val records = db.executionRecordDao().getByDate(dateKey)
        val totalPeople = records.sumOf { it.peopleSeen }
        val totalQueries = records.sumOf { it.queries }
        val totalDeals = records.sumOf { it.deals }

        // 累加结果兜底校验, 防止非法数据写入 settings 进而污染备份
        // (抛异常会回滚外层事务, 不产生脏数据)
        FunnelValidator.validate(totalPeople, totalQueries, totalDeals)

        db.settingDao().setInt(SettingsKeys.peopleSeen(dateKey), totalPeople)
        db.settingDao().setInt(SettingsKeys.queries(dateKey), totalQueries)
        db.settingDao().setInt(SettingsKeys.deals(dateKey), totalDeals)

        // 重算总累计
        recalculateCumulativeTotals()
    }

    /** 重算所有日期的累计总计 */
    private suspend fun recalculateCumulativeTotals() {
        val all = db.settingDao().getAll()
        var totalMeet = 0
        var totalQuery = 0
        var totalDeal = 0

        for (setting in all) {
            val value = setting.value.toIntOrNull() ?: continue
            when {
                setting.key.startsWith("people_seen_") -> totalMeet += value
                setting.key.startsWith("queries_") -> totalQuery += value
                setting.key.startsWith("deals_") -> totalDeal += value
            }
        }

        db.settingDao().setInt(SettingsKeys.TOTAL_MEETS, totalMeet)
        db.settingDao().setInt(SettingsKeys.TOTAL_QUERIES, totalQuery)
        db.settingDao().setInt(SettingsKeys.TOTAL_DEALS, totalDeal)
    }
}
