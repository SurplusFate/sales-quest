package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.entity.DailySummaryEntity

/** 总结内容 */
data class DailySummary(
    val dateKey: String,
    val good: String = "",
    val problems: String = "",
    val customerFeedback: String = "",
    val discovery: String = "",
    val improvement: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isEmpty: Boolean get() = good.isBlank() && problems.isBlank() &&
        customerFeedback.isBlank() && discovery.isBlank() && improvement.isBlank()
}

/**
 * 每日总结服务
 *
 * 总结绑定具体日期 (dateKey), 支持历史日期查看/编辑。
 * 总结只反映数据, 不产生 XP / 任务 / 成就。
 */
class DailySummaryService(
    private val db: AppDatabase,
    private val onDataChanged: () -> Unit = {}
) {

    /** 读取某天总结 */
    suspend fun getSummary(dateKey: String): DailySummary? {
        return db.dailySummaryDao().get(dateKey)?.toModel()
    }

    /** 保存总结 */
    suspend fun saveSummary(summary: DailySummary) {
        db.dailySummaryDao().upsert(
            DailySummaryEntity(
                dateKey = summary.dateKey,
                good = summary.good.trim(),
                problems = summary.problems.trim(),
                customerFeedback = summary.customerFeedback.trim(),
                discovery = summary.discovery.trim(),
                improvement = summary.improvement.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
        onDataChanged()
    }

    /** 删除某天总结 */
    suspend fun deleteSummary(dateKey: String) {
        db.dailySummaryDao().delete(dateKey)
    }

    /** 全部总结 (倒序) */
    suspend fun getAllSummaries(): List<DailySummary> {
        return db.dailySummaryDao().getAll().map { it.toModel() }
    }

    /** 某天的核心数据摘要 */
    suspend fun getDaySummary(dateKey: String): SummarySnapshot {
        val people = db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey))
        val queries = db.settingDao().getInt(SettingsKeys.queries(dateKey))
        val deals = db.settingDao().getInt(SettingsKeys.deals(dateKey))
        val queryRate = if (people > 0) queries * 100.0 / people else 0.0
        val dealRate = if (people > 0) deals * 100.0 / people else 0.0
        return SummarySnapshot(
            dateKey = dateKey,
            peopleSeen = people,
            queries = queries,
            deals = deals,
            queryRate = queryRate,
            dealRate = dealRate
        )
    }

    /** 周期数据快照 (用于周/月总结) */
    suspend fun getRangeSnapshot(startDateKey: String, endDateKey: String): RangeSnapshot {
        val all = db.settingDao().getAll()
        val map = all.associate { it.key to it.value }
        var meet = 0
        var query = 0
        var deal = 0
        var activeDays = 0

        var current = startDateKey
        while (current <= endDateKey) {
            val m = map["people_seen_$current"]?.toIntOrNull() ?: 0
            val q = map["queries_$current"]?.toIntOrNull() ?: 0
            val d = map["deals_$current"]?.toIntOrNull() ?: 0
            meet += m
            query += q
            deal += d
            if (m > 0 || q > 0 || d > 0) activeDays++
            current = nextDateKey(current)
        }

        return RangeSnapshot(meet, query, deal, activeDays)
    }
}

/** 单日数据摘要 (总结页上方自动显示) */
data class SummarySnapshot(
    val dateKey: String,
    val peopleSeen: Int,
    val queries: Int,
    val deals: Int,
    val queryRate: Double,
    val dealRate: Double
)

/** 周期累计快照 */
data class RangeSnapshot(
    val peopleSeen: Int,
    val queries: Int,
    val deals: Int,
    val activeDays: Int
) {
    val queryRate: Double get() = if (peopleSeen > 0) queries * 100.0 / peopleSeen else 0.0
    val dealRate: Double get() = if (peopleSeen > 0) deals * 100.0 / peopleSeen else 0.0
}

private fun DailySummaryEntity.toModel() = DailySummary(
    dateKey = dateKey,
    good = good,
    problems = problems,
    customerFeedback = customerFeedback,
    discovery = discovery,
    improvement = improvement,
    updatedAt = updatedAt
)

/** dateKey 次日 */
internal fun nextDateKey(dateKey: String): String {
    val parts = dateKey.split("-").map { it.toInt() }
    val c = java.util.Calendar.getInstance()
    c.clear()
    c.set(parts[0], parts[1] - 1, parts[2])
    c.add(java.util.Calendar.DAY_OF_MONTH, 1)
    return DateUtil.dateKey(c.timeInMillis)
}
