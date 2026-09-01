package com.salesquest.sales_quest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.entity.ExecutionRecordEntity
import com.salesquest.sales_quest.services.ExecutionRecordService
import com.salesquest.sales_quest.ui.BattleStats
import com.salesquest.sales_quest.ui.ExecutionRecordUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 执行记录列表页 ViewModel — 管理选中日期的执行记录列表 + 当天累计
 *
 * 功能:
 * - 切换日期查看该天所有执行记录
 * - 当天累计 (从记录自动计算)
 * - 有记录的历史日期列表
 * - 编辑/删除单条记录
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExecutionRecordViewModel : ViewModel() {

    private val service = AppContainer.executionRecordService

    /** 当前选中的日期 */
    private val selectedDateKeyFlow = MutableStateFlow(DateUtil.dateKey())

    /** 选中日期的执行记录 (响应式) */
    val records: StateFlow<List<ExecutionRecordUi>> = selectedDateKeyFlow
        .flatMapLatest { dateKey ->
            service.watchRecords(dateKey).map { list -> list.map { it.toUiModel() } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 选中日期的当天累计 */
    val dailyTotal: StateFlow<BattleStats> = selectedDateKeyFlow
        .flatMapLatest { dateKey ->
            service.watchRecords(dateKey).map { list ->
                BattleStats(
                    peopleSeen = list.sumOf { it.peopleSeen },
                    queries = list.sumOf { it.queries },
                    deals = list.sumOf { it.deals }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BattleStats())

    /** 当前选中日期 */
    val selectedDateKey: StateFlow<String> = selectedDateKeyFlow

    /** 所有有执行记录的日期 (按日期倒序) */
    private val allDatesFlow = MutableStateFlow<List<String>>(emptyList())
    val allDates: StateFlow<List<String>> = allDatesFlow

    init {
        refreshAllDates()
    }

    /** 刷新历史日期列表 */
    private fun refreshAllDates() {
        viewModelScope.launch {
            try {
                allDatesFlow.value = service.getAllDates()
            } catch (_: Exception) { }
        }
    }

    /** 切换选中日期 */
    fun selectDate(dateKey: String) {
        selectedDateKeyFlow.value = dateKey
        refreshAllDates()
    }

    /** 删除一条记录 */
    fun deleteRecord(id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                service.deleteRecord(id)
                refreshAllDates()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /** 更新一条记录 */
    fun updateRecord(
        id: String,
        peopleSeen: Int,
        queries: Int,
        deals: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                service.updateRecord(id, peopleSeen, queries, deals)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /** ExecutionRecordEntity → ExecutionRecordUi */
    private fun ExecutionRecordEntity.toUiModel() = ExecutionRecordUi(
        id = id,
        dateKey = dateKey,
        timeLabel = HomeViewModel.formatTimeLabel(this),
        timePrecision = timePrecision,
        peopleSeen = peopleSeen,
        queries = queries,
        deals = deals
    )
}
