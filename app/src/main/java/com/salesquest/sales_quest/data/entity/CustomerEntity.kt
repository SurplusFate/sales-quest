package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 客户表 (PRD §25)
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = "",
    val name: String,
    val phone: String = "",
    val operator: String = "UNKNOWN",
    val selfReportedCost: Int? = null,
    val actualCost: Int? = null,
    val packageName: String? = null,
    val traffic: String? = null,
    val minutes: String? = null,
    val broadband: Boolean = false,
    val subCards: Int = 0,
    val camera: Boolean = false,
    val contractStatus: String? = null,
    val otherBusiness: String? = null,
    val status: String = "INVALID",
    val valueScore: Int = 0,
    val valueLevel: String = "LOW",
    val salesStage: String = "NEW",
    val nextAction: String? = null,
    val nextFollowUpAt: Long? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
