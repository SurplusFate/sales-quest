package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日总结表 (v2)
 * 总结绑定具体日期 dateKey (yyyy-MM-dd)
 */
@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val dateKey: String,
    val good: String = "",
    val problems: String = "",
    val customerFeedback: String = "",
    val discovery: String = "",
    val improvement: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
