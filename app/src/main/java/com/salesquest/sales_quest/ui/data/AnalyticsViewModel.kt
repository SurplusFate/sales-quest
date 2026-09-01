package com.salesquest.sales_quest.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.ui.BattleStats
import com.salesquest.sales_quest.ui.TotalStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 数据分析 ViewModel - 任意历史日期查看/录入/修改 + 本周/本月/累计数据 */
class AnalyticsViewModel : ViewModel() {

    private val db = AppContainer.db
    private val statsService = AppContainer.dailyStatsService

    private val settingsFlow = db.settingDao().watchAll()

    /** 当前选中的日期 (默认今天, 可切换任意过去日期) */
    private val selectedDateKeyFlow = MutableStateFlow(DateUtil.dateKey())

    /** 当前选中日期的数据 */
    val selectedStats: StateFlow<BattleStats> = combine(settingsFlow, selectedDateKeyFlow) { settings, dateKey ->
        val map = settings.associate { it.key to it.value }
        BattleStats(
            peopleSeen = map[SettingsKeys.peopleSeen(dateKey)]?.toIntOrNull() ?: 0,
            queries = map[SettingsKeys.queries(dateKey)]?.toIntOrNull() ?: 0,
            deals = map[SettingsKeys.deals(dateKey)]?.toIntOrNull() ?: 0
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

    /** 本周累计 (周一 ~ 今天, 与首页共用 settings 数据源) */
    val weekStats: StateFlow<BattleStats> = settingsFlow.map { settings ->
        sumRange(settings, DateUtil.dateKeysBetween(DateUtil.mondayStart(), System.currentTimeMillis()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BattleStats())

    /** 本月累计 (1号 ~ 今天) */
    val monthStats: StateFlow<BattleStats> = settingsFlow.map { settings ->
        sumRange(settings, DateUtil.dateKeysBetween(DateUtil.monthStart(), System.currentTimeMillis()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BattleStats())

    /** 今日执行度 = 参与任务的进度均值 */
    val executionRate: StateFlow<Double> = db.taskDao().watchByDate(DateUtil.dateKey()).map { tasks: List<DailyTaskEntity> ->
        if (tasks.isEmpty()) return@map 0.0
        var sum = 0.0
        for (task in tasks) {
            val rate = if (task.target <= 0) 0.0
            else (task.progress.toDouble() / task.target).coerceIn(0.0, 1.0)
            sum += rate
        }
        sum / tasks.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ==================== 操作 ====================

    /** 切换选中日期 */
    fun selectDate(dateKey: String) {
        selectedDateKeyFlow.value = dateKey
    }

    /** 当前选中日期 */
    fun currentDateKey(): String = selectedDateKeyFlow.value

    /**
     * 整组保存某天数据 (历史录入/修改)
     * 校验失败时抛出 IllegalArgumentException (中文提示)
     */
    suspend fun updateDailyStats(dateKey: String, peopleSeen: Int, queries: Int, deals: Int) {
        statsService.updateDailyStats(dateKey, peopleSeen, queries, deals)
    }

    /** 单指标更新某天数据 (供编辑对话框) */
    suspend fun updateDailyMetric(dateKey: String, metricCode: String, newValue: Int) {
        statsService.updateDailyMetric(dateKey, metricCode, newValue)
    }

    /**
     * 历史编辑: 读取当前日期的其它指标值, 组合成整组后校验并保存
     * 校验失败时抛出 IllegalArgumentException (中文提示)
     */
    suspend fun editDailyMetric(dateKey: String, metricCode: String, newValue: Int) {
        if (newValue < 0) throw IllegalArgumentException("数字不能为负数")
        val current = statsService.getDailyStats(dateKey)
        val peopleSeen = if (metricCode == "MEET") newValue else current.peopleSeen
        val queries = if (metricCode == "QUERY") newValue else current.queries
        val deals = if (metricCode == "DEAL") newValue else current.deals
        statsService.updateDailyStats(dateKey, peopleSeen, queries, deals)
    }

    companion object {
        /** 累加指定日期区间内的每日明细 (people_seen_/queries_/deals_ 前缀键) */
        internal fun sumRange(settings: List<SettingEntity>, dateKeys: List<String>): BattleStats {
            val map = settings.associate { it.key to it.value }
            var peopleSeen = 0
            var queries = 0
            var deals = 0
            for (d in dateKeys) {
                peopleSeen += map[SettingsKeys.peopleSeen(d)]?.toIntOrNull() ?: 0
                queries += map[SettingsKeys.queries(d)]?.toIntOrNull() ?: 0
                deals += map[SettingsKeys.deals(d)]?.toIntOrNull() ?: 0
            }
            return BattleStats(peopleSeen = peopleSeen, queries = queries, deals = deals)
        }
    }
}
