package com.salesquest.sales_quest.ui

import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.services.DailyTaskConfig

/** V1.0 作战数据 - 只有三个核心数字 */
data class BattleStats(
    val peopleSeen: Int = 0,
    val queries: Int = 0,
    val deals: Int = 0
)

/** 累计统计 */
data class TotalStats(
    val totalMeet: Int = 0,
    val totalQuery: Int = 0,
    val totalDeal: Int = 0
)

/** 首页组合状态 */
data class HomeUiState(
    val stats: BattleStats = BattleStats(),
    val tasks: List<DailyTaskEntity> = emptyList(),
    val config: DailyTaskConfig? = null,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val loading: Boolean = true
)
