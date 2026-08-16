package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户统计表
 */
@Entity(tableName = "user_stats")
data class UserStatEntity(
    @PrimaryKey val id: String = "default",
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val streakDays: Int = 0,
    val lastActiveDate: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
