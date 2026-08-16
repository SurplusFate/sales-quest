package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * XP 记录表 (PRD §25)
 */
@Entity(tableName = "xp_records")
data class XpRecordEntity(
    @PrimaryKey val id: String = "",
    val customerId: String? = null,
    val actionType: String,
    val xp: Int,
    val createdAt: Long = System.currentTimeMillis()
)
