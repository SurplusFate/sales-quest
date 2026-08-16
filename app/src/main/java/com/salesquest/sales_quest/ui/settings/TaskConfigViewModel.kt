package com.salesquest.sales_quest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.services.DailyTaskConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 任务配置 ViewModel */
class TaskConfigViewModel : ViewModel() {

    val config: MutableStateFlow<DailyTaskConfig?> = MutableStateFlow(null)
    val locked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            try {
                config.value = AppContainer.dailyTaskService.getTodayConfig()
                locked.value = AppContainer.dailyTaskService.isTodayLocked()
            } finally {
                loading.value = false
            }
        }
    }

    suspend fun save(
        meetTarget: Int,
        queryTarget: Int,
        dealTarget: Int,
        includeMeet: Boolean,
        includeQuery: Boolean,
        includeDeal: Boolean
    ) {
        val cfg = DailyTaskConfig(
            meetTarget = meetTarget,
            queryTarget = queryTarget,
            dealTarget = dealTarget,
            includeMeet = includeMeet,
            includeQuery = includeQuery,
            includeDeal = includeDeal
        )
        AppContainer.dailyTaskService.setDayConfig(System.currentTimeMillis(), cfg)
        config.value = cfg
        locked.value = AppContainer.dailyTaskService.isTodayLocked()
    }
}
