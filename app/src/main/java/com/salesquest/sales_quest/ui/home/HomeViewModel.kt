package com.salesquest.sales_quest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.ui.BattleStats
import com.salesquest.sales_quest.ui.HomeUiState
import com.salesquest.sales_quest.ui.WeekDayStats
import com.salesquest.sales_quest.services.DailyTaskConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 首页 ViewModel - 组合今日作战数据/任务/等级/连续作战 */
class HomeViewModel : ViewModel() {

    private val db = AppContainer.db

    private val todayDateKey = DateUtil.dateKey()

    private val battleStatsFlow = db.settingDao().watchAll().map { settings ->
        val map = settings.associate { it.key to it.value }
        BattleStats(
            peopleSeen = map["people_seen_$todayDateKey"]?.toIntOrNull() ?: 0,
            queries = map["queries_$todayDateKey"]?.toIntOrNull() ?: 0,
            deals = map["deals_$todayDateKey"]?.toIntOrNull() ?: 0
        )
    }

    /** 本周战绩 (周一~周六, 与数据分析页共用 settings 数据源) */
    private val weekStatsFlow = db.settingDao().watchAll().map { settings ->
        buildWeekStats(settings)
    }

    private val tasksFlow = db.taskDao().watchByDate(todayDateKey)

    private val statsFlow = db.statsDao().watchStats()

    private val configFlow = MutableStateFlow<DailyTaskConfig?>(null)

    init {
        viewModelScope.launch {
            configFlow.value = AppContainer.dailyTaskService.getTodayConfig()
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        battleStatsFlow,
        tasksFlow,
        statsFlow,
        configFlow,
        weekStatsFlow
    ) { stats, tasks, userStats, config, weekStats ->
        HomeUiState(
            stats = stats,
            tasks = tasks,
            config = config,
            totalXp = userStats?.totalXp ?: 0,
            streakDays = userStats?.streakDays ?: 0,
            weekStats = weekStats,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    companion object {
        /**
         * 由 settings 列表组装周一至周六的本周战绩
         * 与 DailyStatsService 共用同一 settings 数据源, 保证两页数据一致
         */
        internal fun buildWeekStats(settings: List<com.salesquest.sales_quest.data.entity.SettingEntity>): List<WeekDayStats> {
            val map = settings.associate { it.key to it.value }
            return DateUtil.weekDateKeys().map { dateKey ->
                WeekDayStats(
                    dateKey = dateKey,
                    weekday = DateUtil.weekdayName(dateKey),
                    dateLabel = DateUtil.monthDayLabel(dateKey),
                    stats = BattleStats(
                        peopleSeen = map["people_seen_$dateKey"]?.toIntOrNull() ?: 0,
                        queries = map["queries_$dateKey"]?.toIntOrNull() ?: 0,
                        deals = map["deals_$dateKey"]?.toIntOrNull() ?: 0
                    )
                )
            }
        }
    }
}
