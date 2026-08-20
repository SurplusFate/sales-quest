package com.salesquest.sales_quest.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.entity.CustomerEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** 客户列表 ViewModel */
class CustomerListViewModel : ViewModel() {
    // 性能优化: 在 ViewModel 层过滤已成交客户, 避免每次 Composable 重组都 filter 一次
    val customers: StateFlow<List<CustomerEntity>> = AppContainer.db.customerDao().watchAll()
        .map { list -> list.filter { CustomerStage.fromCode(it.salesStage) != CustomerStage.WON } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
