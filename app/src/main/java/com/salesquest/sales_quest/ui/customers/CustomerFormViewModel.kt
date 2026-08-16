package com.salesquest.sales_quest.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.entity.CustomerEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** 客户表单 ViewModel (编辑时预填数据) */
class CustomerFormViewModel(private val customerId: String?) : ViewModel() {

    val customer: StateFlow<CustomerEntity?> =
        if (customerId != null) {
            AppContainer.db.customerDao().watchById(customerId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } else {
            MutableStateFlow(null)
        }

    companion object {
        fun factory(customerId: String?): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CustomerFormViewModel(customerId) as T
            }
        }
    }
}
