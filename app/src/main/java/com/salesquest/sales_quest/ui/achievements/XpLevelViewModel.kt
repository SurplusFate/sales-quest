package com.salesquest.sales_quest.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.services.LevelService
import com.salesquest.sales_quest.services.LevelProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 等级页 ViewModel - 晋级条件进度 (响应式, 数据变化时自动刷新) */
class XpLevelViewModel : ViewModel() {

    private val db = AppContainer.db
    private val levelService = AppContainer.levelService

    /** 等级进度: 由 stats + settings 数据变化驱动, 使用 LevelService 多条件判定 */
    val progress: StateFlow<LevelProgress?> = combine(
        db.statsDao().watchStats(),
        db.settingDao().watchAll()
    ) { stats, settings ->
        val map = settings.associate { it.key to it.value }
        val totalXp = stats?.totalXp ?: 0
        val totalMeet = map[SettingsKeys.TOTAL_MEETS]?.toIntOrNull() ?: 0
        val totalQuery = map[SettingsKeys.TOTAL_QUERIES]?.toIntOrNull() ?: 0
        val totalDeal = map[SettingsKeys.TOTAL_DEALS]?.toIntOrNull() ?: 0
        val streakDays = stats?.streakDays ?: 0

        val requirements = levelService.getRequirements()
        LevelService.buildProgress(requirements, totalXp, totalMeet, totalQuery, totalDeal, streakDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 总 XP (从等级进度中提取, 保证 UI 与业务逻辑一致) */
    val totalXp: StateFlow<Int> = progress.map { it?.totalXp ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
