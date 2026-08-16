package com.salesquest.sales_quest.ui.settings

import androidx.lifecycle.ViewModel
import com.salesquest.sales_quest.core.AppContainer

/** 设置页 ViewModel - 数据清除操作 */
class SettingsViewModel : ViewModel() {

    suspend fun clearToday() {
        AppContainer.dailyTaskService.clearTodayData()
    }

    suspend fun clearAll() {
        AppContainer.db.clearAllData()
    }
}
