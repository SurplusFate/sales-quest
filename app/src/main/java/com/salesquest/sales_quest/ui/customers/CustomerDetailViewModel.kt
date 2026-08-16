package com.salesquest.sales_quest.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.entity.CustomerEntity
import com.salesquest.sales_quest.data.entity.FollowUpEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 客户详情 ViewModel */
class CustomerDetailViewModel(private val customerId: String) : ViewModel() {

    val customer: StateFlow<CustomerEntity?> = AppContainer.db.customerDao().watchById(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val followUps: StateFlow<List<FollowUpEntity>> = AppContainer.db.followUpDao().watchByCustomer(customerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun factory(customerId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CustomerDetailViewModel(customerId) as T
            }
        }
    }
}
