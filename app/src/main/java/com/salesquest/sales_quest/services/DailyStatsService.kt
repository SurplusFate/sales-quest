package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.ui.BattleStats

/**
 * 每日统计数据服务 - 任意历史日期的查看/录入/修改
 *
 * 核心原则:
 * 1. 历史数据修改只影响统计数字, 不产生任何 XP / 任务 / 成就
 * 2. 累计值始终等于所有日期数据之和 (recalculateTotals)
 * 3. 数据校验: 0 <= 成交 <= 查询 <= 见人, 不允许负数
 */
class DailyStatsService(private val db: AppDatabase) {

    /** 读取某天数据 */
    suspend fun getDailyStats(dateKey: String): BattleStats = BattleStats(
        peopleSeen = db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)),
        queries = db.settingDao().getInt(SettingsKeys.queries(dateKey)),
        deals = db.settingDao().getInt(SettingsKeys.deals(dateKey))
    )

    /**
     * 整组更新某天数据 (历史录入/修改)
     *
     * 1. 参数校验 (负数 / 0 <= 成交 <= 查询 <= 见人)
     * 2. 写入每日明细
     * 3. 重算累计
     * 4. 不产生 XP, 不触发任务/成就
     */
    suspend fun updateDailyStats(dateKey: String, peopleSeen: Int, queries: Int, deals: Int) {
        validateDailyStats(peopleSeen, queries, deals)

        db.settingDao().setInt(SettingsKeys.peopleSeen(dateKey), peopleSeen)
        db.settingDao().setInt(SettingsKeys.queries(dateKey), queries)
        db.settingDao().setInt(SettingsKeys.deals(dateKey), deals)

        recalculateTotals()
    }

    /**
     * 单指标更新 (供实时录入/今日编辑复用)
     * 负数拒绝; 不校验指标间关系 (允许分步录入)
     */
    suspend fun updateDailyMetric(dateKey: String, metricCode: String, newValue: Int) {
        if (newValue < 0) throw IllegalArgumentException("数字不能为负数")
        when (metricCode) {
            "MEET" -> db.settingDao().setInt(SettingsKeys.peopleSeen(dateKey), newValue)
            "QUERY" -> db.settingDao().setInt(SettingsKeys.queries(dateKey), newValue)
            "DEAL" -> db.settingDao().setInt(SettingsKeys.deals(dateKey), newValue)
            else -> throw IllegalArgumentException("未知指标: $metricCode")
        }
        recalculateTotals()
    }

    /**
     * 整组数据校验
     * 规则: 0 <= 成交 <= 查询 <= 见人, 不允许负数
     * @throws IllegalArgumentException 校验失败时抛出, 消息为中文提示
     */
    fun validateDailyStats(peopleSeen: Int, queries: Int, deals: Int) {
        if (peopleSeen < 0 || queries < 0 || deals < 0) {
            throw IllegalArgumentException("数字不能为负数")
        }
        if (queries > peopleSeen) {
            throw IllegalArgumentException("查询数不能超过见人数")
        }
        if (deals > queries) {
            throw IllegalArgumentException("成交数不能超过查询数")
        }
    }

    /**
     * 数据校正: 遍历所有历史日期明细, 重算累计值
     * 累计 = 所有日期之和, 不依赖手工增减
     */
    suspend fun recalculateTotals() {
        var totalMeet = 0
        var totalQuery = 0
        var totalDeal = 0

        val all = db.settingDao().getAll()
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
