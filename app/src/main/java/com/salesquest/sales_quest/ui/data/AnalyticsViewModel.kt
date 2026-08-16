package com.salesquest.sales_quest.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.ui.BattleStats
import com.salesquest.sales_quest.ui.TotalStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 数据分析 ViewModel - 今日/累计数据 + 今日执行度 */
class AnalyticsViewModel : ViewModel() {

    private val db = AppContainer.db
    private val todayDateKey = DateUtil.dateKey()

    private val settingsFlow = db.settingDao().watchAll()

    val today: StateFlow<BattleStats> = settingsFlow.map { settings ->
        val map = settings.associate { it.key to it.value }
        BattleStats(
            peopleSeen = map[SettingsKeys.peopleSeen(todayDateKey)]?.toIntOrNull() ?: 0,
            queries = map[SettingsKeys.queries(todayDateKey)]?.toIntOrNull() ?: 0,
            deals = map[SettingsKeys.deals(todayDateKey)]?.toIntOrNull() ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BattleStats())

    val total: StateFlow<TotalStats> = settingsFlow.map { settings ->
        val map = settings.associate { it.key to it.value }
        TotalStats(
            totalMeet = map[SettingsKeys.TOTAL_MEETS]?.toIntOrNull() ?: 0,
            totalQuery = map[SettingsKeys.TOTAL_QUERIES]?.toIntOrNull() ?: 0,
            totalDeal = map[SettingsKeys.TOTAL_DEALS]?.toIntOrNull() ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TotalStats())

    /** 今日执行度 = 参与任务的进度均值 */
    val executionRate: StateFlow<Double> = db.taskDao().watchByDate(todayDateKey).map { tasks: List<DailyTaskEntity> ->
        if (tasks.isEmpty()) return@map 0.0
        var sum = 0.0
        for (task in tasks) {
            val rate = if (task.target <= 0) 0.0
            else (task.progress.toDouble() / task.target).coerceIn(0.0, 1.0)
            sum += rate
        }
        sum / tasks.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
