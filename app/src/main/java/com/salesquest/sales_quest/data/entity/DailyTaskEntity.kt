package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日任务表
 */
@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey val id: String = "",
    val date: String, // yyyy-MM-dd
    val taskId: String, // 对应 DailyTaskDef.id
    val tier: String,
    val metric: String,
    val target: Int,
    val progress: Int = 0,
    val completed: Boolean = false,
    val xpReward: Int,
    val createdAt: Long = System.currentTimeMillis()
)
