package com.salesquest.sales_quest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.LevelService
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

/**
 * 首页 ViewModel - 组合今日作战数据/任务/等级/连续作战
 *
 * 性能优化: 共享单一 settingsFlow 订阅, 避免 3 次 watchAll() 重复订阅
 */
class HomeViewModel : ViewModel() {

    private val db = AppContainer.db
    private val levelService = AppContainer.levelService

    private val todayDateKey = DateUtil.dateKey()

    // 单一 settings 订阅源 (之前 watchAll 被调用 3 次, 每次任意 setting 变化都触发 3 路重新计算)
    private val settingsFlow = db.settingDao().watchAll()

    // 从共享 settingsFlow 派生今日战绩 (仅当数据真正变化时才 emit)
    private val battleStatsFlow = settingsFlow.map { settings ->
        val map = settings.associate { it.key to it.value }
        BattleStats(
            peopleSeen = map["people_seen_$todayDateKey"]?.toIntOrNull() ?: 0,
            queries = map["queries_$todayDateKey"]?.toIntOrNull() ?: 0,
            deals = map["deals_$todayDateKey"]?.toIntOrNull() ?: 0
        )
    }

    /** 本周战绩 (周一~周六, 与数据分析页共用 settings 数据源) */
    private val weekStatsFlow = settingsFlow.map { settings ->
        buildWeekStats(settings)
    }

    private val tasksFlow = db.taskDao().watchByDate(todayDateKey)

    private val statsFlow = db.statsDao().watchStats()

    /** 等级进度: 使用 LevelService 多条件判定 (非纯 XP) */
    private val levelProgressFlow = combine(
        statsFlow,
        settingsFlow
    ) { stats, settings ->
        val map = settings.associate { it.key to it.value }
        val totalXp = stats?.totalXp ?: 0
        val totalMeet = map[SettingsKeys.TOTAL_MEETS]?.toIntOrNull() ?: 0
        val totalQuery = map[SettingsKeys.TOTAL_QUERIES]?.toIntOrNull() ?: 0
        val totalDeal = map[SettingsKeys.TOTAL_DEALS]?.toIntOrNull() ?: 0
        val streakDays = stats?.streakDays ?: 0

        val requirements = levelService.getRequirements()
        LevelService.buildProgress(requirements, totalXp, totalMeet, totalQuery, totalDeal, streakDays)
    }

    private val configFlow = MutableStateFlow<DailyTaskConfig?>(null)

    init {
        viewModelScope.launch {
            configFlow.value = AppContainer.dailyTaskService.getTodayConfig()
        }
    }

    // Kotlin combine 只支持 5 个有类型重载; 6 个流需嵌套
    private val statsTasksPair = combine(battleStatsFlow, tasksFlow) { stats, tasks -> stats to tasks }
    private val statsConfigPair = combine(statsFlow, configFlow) { userStats, config -> userStats to config }
    private val weekLevelPair = combine(weekStatsFlow, levelProgressFlow) { weekStats, levelProgress -> weekStats to levelProgress }

    val uiState: StateFlow<HomeUiState> = combine(
        statsTasksPair,
        statsConfigPair,
        weekLevelPair
    ) { st, sc, wl ->
        HomeUiState(
            stats = st.first,
            tasks = st.second,
            config = sc.second,
            totalXp = sc.first?.totalXp ?: 0,
            streakDays = sc.first?.streakDays ?: 0,
            weekStats = wl.first,
            levelProgress = wl.second,
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
