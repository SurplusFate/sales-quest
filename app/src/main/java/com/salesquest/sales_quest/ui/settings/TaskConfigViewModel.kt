package com.salesquest.sales_quest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.services.DailyTaskConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 任务配置 ViewModel — 响应式读取今日配置, 保存后自动反映最新值 */
class TaskConfigViewModel : ViewModel() {

    /** 响应式配置: settings 表变更 → Flow emit → UI 自动重组 */
    val config: StateFlow<DailyTaskConfig?> =
        AppContainer.dailyTaskService.watchTodayConfig()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init {
        viewModelScope.launch {
            try {
                _locked.value = AppContainer.dailyTaskService.isTodayLocked()
            } finally {
                _loading.value = false
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
        _locked.value = AppContainer.dailyTaskService.isTodayLocked()
    }
}
