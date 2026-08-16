package com.salesquest.sales_quest.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 等级页 ViewModel - 监听总 XP */
class XpLevelViewModel : ViewModel() {

    val totalXp: StateFlow<Int> = AppContainer.db.statsDao().watchStats()
        .map { it?.totalXp ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
