package com.salesquest.sales_quest.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.services.LevelProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 等级页 ViewModel - 晋级条件进度 */
class XpLevelViewModel : ViewModel() {

    val totalXp: StateFlow<Int> = AppContainer.db.statsDao().watchStats()
        .combine(AppContainer.db.settingDao().watchAll()) { stats, _ ->
            stats?.totalXp ?: 0
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private val _progress = MutableStateFlow<LevelProgress?>(null)
    val progress: StateFlow<LevelProgress?> = _progress

    init {
        viewModelScope.launch {
            _progress.value = AppContainer.levelService.getProgress()
        }
    }
}
