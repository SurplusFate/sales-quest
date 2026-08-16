package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 客户事件表 (PRD §25)
 */
@Entity(tableName = "customer_events")
data class CustomerEventEntity(
    @PrimaryKey val id: String = "",
    val customerId: String,
    val eventType: String,
    val eventTime: Long = System.currentTimeMillis(),
    val note: String? = null,
    val metadata: String? = null
)
