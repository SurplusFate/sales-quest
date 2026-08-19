package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil

/** 单周统计 */
data class WeekSummary(
    val weekLabel: String,
    val peopleSeen: Int,
    val queries: Int,
    val deals: Int,
    val queryRate: Double,
    val dealRate: Double,
    val activeDays: Int
)

/** 周对比结果 */
data class WeekComparison(
    val current: WeekSummary,
    val previous: WeekSummary,
    val meetChangePercent: Double?,
    val queryRateChangePercent: Double?,
    val analysis: String
)

/**
 * 周/月总结分析服务
 *
 * 基于 settings 中每日明细统计, 输出:
 * 1. 本周核心数据 (见人/查询/成交/查询率/成交率/活跃天数)
 * 2. 与上周对比 (变化百分比)
 * 3. 简单规则分析 (不依赖 AI)
 */
class WeeklySummaryService(private val db: AppDatabase) {

    private val dailySummaryService = DailySummaryService(db)

    /** 本周 vs 上周对比分析 */
    suspend fun compareThisWeek(): WeekComparison {
        val thisWeekStart = DateUtil.mondayStart()
        val thisWeekEnd = thisWeekStart + 7 * 24L * 60 * 60 * 1000
        val prevWeekStart = thisWeekStart - 7 * 24L * 60 * 60 * 1000
        val prevWeekEnd = thisWeekStart

        val current = buildWeekSummary(thisWeekStart, thisWeekEnd, "本周")
        val previous = buildWeekSummary(prevWeekStart, prevWeekEnd, "上周")

        val meetChange = percentChange(current.peopleSeen.toDouble(), previous.peopleSeen.toDouble())
        val queryRateChange = percentChange(current.queryRate, previous.queryRate)

        val analysis = analyze(current, previous, meetChange, queryRateChange)
        return WeekComparison(current, previous, meetChange, queryRateChange, analysis)
    }

    private suspend fun buildWeekSummary(start: Long, end: Long, label: String): WeekSummary {
        val snapshot = dailySummaryService.getRangeSnapshot(
            startDateKey = DateUtil.dateKey(start),
            endDateKey = DateUtil.dateKey(end - 1)
        )
        return WeekSummary(
            weekLabel = label,
            peopleSeen = snapshot.peopleSeen,
            queries = snapshot.queries,
            deals = snapshot.deals,
            queryRate = snapshot.queryRate,
            dealRate = snapshot.dealRate,
            activeDays = snapshot.activeDays
        )
    }

    private fun percentChange(current: Double, previous: Double): Double? {
        return if (previous <= 0) null else (current - previous) * 100.0 / previous
    }

    /** 简单规则分析 */
    private fun analyze(
        current: WeekSummary,
        previous: WeekSummary,
        meetChange: Double?,
        queryRateChange: Double?
    ): String {
        val parts = mutableListOf<String>()
        if (current.peopleSeen == 0 && current.queries == 0 && current.deals == 0) {
            return "本周暂无数据, 继续加油!"
        }

        meetChange?.let { change ->
            val pct = Math.abs(change)
            parts.add(
                if (change < 0) "本周见人较上周下降 ${formatPct(pct)}"
                else "本周见人较上周提升 ${formatPct(pct)}"
            )
        } ?: run {
            parts.add("本周见人 ${current.peopleSeen} 人")
        }

        queryRateChange?.let { change ->
            val pct = Math.abs(change)
            parts.add(
                if (change >= 0) "查询率提高 ${formatPct(pct)}"
                else "查询率下降 ${formatPct(pct)}"
            )
        }

        // 规则推导: 见人下降但查询率提升 → 沟通质量提升
        val meetDown = meetChange != null && meetChange < 0
        val queryRateUp = queryRateChange != null && queryRateChange >= 0
        if (meetDown && queryRateUp) {
            parts.add("虽然接触人数减少, 但有效沟通质量有所提高")
        } else if (meetDown) {
            parts.add("建议增加外访频次, 扩大接触面")
        } else if (queryRateUp) {
            parts.add("有效沟通保持良好")
        }

        return parts.joinToString("; ")
    }

    private fun formatPct(value: Double): String = String.format("%.1f%%", value)
}
