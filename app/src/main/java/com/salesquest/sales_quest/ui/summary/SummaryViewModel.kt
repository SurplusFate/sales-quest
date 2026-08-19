package com.salesquest.sales_quest.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.DailySummary
import com.salesquest.sales_quest.services.SummarySnapshot
import com.salesquest.sales_quest.services.WeekComparison
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 每日总结编辑表单状态 */
data class SummaryFormState(
    val dateKey: String = DateUtil.dateKey(),
    val good: String = "",
    val problems: String = "",
    val customerFeedback: String = "",
    val discovery: String = "",
    val improvement: String = "",
    val loaded: Boolean = false
)

/** 总结页状态 */
data class SummaryUiState(
    val form: SummaryFormState = SummaryFormState(),
    val snapshot: SummarySnapshot? = null,
    val history: List<DailySummary> = emptyList(),
    val weekComparison: WeekComparison? = null,
    val loading: Boolean = true,
    val message: String? = null
)

/** 总结页 ViewModel - 每日总结 + 历史列表 + 周对比 */
class SummaryViewModel : ViewModel() {

    private val service get() = AppContainer.dailySummaryService
    private val weeklyService get() = AppContainer.weeklySummaryService

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _uiState.value
            loadForDate(current.form.dateKey)
            _uiState.value = _uiState.value.copy(
                history = service.getAllSummaries(),
                weekComparison = weeklyService.compareThisWeek(),
                loading = false
            )
        }
    }

    fun selectDate(dateKey: String) {
        viewModelScope.launch {
            loadForDate(dateKey)
        }
    }

    private suspend fun loadForDate(dateKey: String) {
        val existing = service.getSummary(dateKey)
        val snapshot = service.getDaySummary(dateKey)
        _uiState.value = _uiState.value.copy(
            form = SummaryFormState(
                dateKey = dateKey,
                good = existing?.good ?: "",
                problems = existing?.problems ?: "",
                customerFeedback = existing?.customerFeedback ?: "",
                discovery = existing?.discovery ?: "",
                improvement = existing?.improvement ?: "",
                loaded = true
            ),
            snapshot = snapshot
        )
    }

    fun onGoodChange(v: String) = updateForm { it.copy(good = v) }
    fun onProblemsChange(v: String) = updateForm { it.copy(problems = v) }
    fun onFeedbackChange(v: String) = updateForm { it.copy(customerFeedback = v) }
    fun onDiscoveryChange(v: String) = updateForm { it.copy(discovery = v) }
    fun onImprovementChange(v: String) = updateForm { it.copy(improvement = v) }

    private fun updateForm(transform: (SummaryFormState) -> SummaryFormState) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun save() {
        viewModelScope.launch {
            val f = _uiState.value.form
            service.saveSummary(
                DailySummary(
                    dateKey = f.dateKey,
                    good = f.good,
                    problems = f.problems,
                    customerFeedback = f.customerFeedback,
                    discovery = f.discovery,
                    improvement = f.improvement
                )
            )
            _uiState.value = _uiState.value.copy(
                history = service.getAllSummaries(),
                message = "总结已保存"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
