package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 跟进表
 */
@Entity(tableName = "follow_ups")
data class FollowUpEntity(
    @PrimaryKey val id: String = "",
    val customerId: String,
    val scheduledAt: Long,
    val content: String? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
