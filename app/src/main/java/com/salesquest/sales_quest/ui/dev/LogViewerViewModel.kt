package com.salesquest.sales_quest.ui.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.core.LogEntry
import com.salesquest.sales_quest.core.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/** 日志页 ViewModel - 定时刷新触发重组 */
class LogViewerViewModel : ViewModel() {

    private val _version = MutableStateFlow(0)
    val version: StateFlow<Int> = _version

    init {
        viewModelScope.launch {
            while (true) {
                delay(500)
                _version.value++
            }
        }
    }

    fun filtered(keyword: String, level: LogLevel?): List<LogEntry> {
        var result = AppLogger.entries
        if (level != null) {
            val minIndex = LogLevel.entries.indexOf(level)
            result = result.filter { LogLevel.entries.indexOf(it.level) >= minIndex }
        }
        if (keyword.isNotEmpty()) {
            val lower = keyword.lowercase(Locale.getDefault())
            result = result.filter {
                it.message.lowercase().contains(lower) || it.tag.lowercase().contains(lower)
            }
        }
        return result
    }
}
