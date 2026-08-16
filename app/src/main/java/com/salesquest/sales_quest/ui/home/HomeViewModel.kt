package com.salesquest.sales_quest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.ui.BattleStats
import com.salesquest.sales_quest.ui.HomeUiState
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
        configFlow
    ) { stats, tasks, userStats, config ->
        HomeUiState(
            stats = stats,
            tasks = tasks,
            config = config,
            totalXp = userStats?.totalXp ?: 0,
            streakDays = userStats?.streakDays ?: 0,
            loading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
