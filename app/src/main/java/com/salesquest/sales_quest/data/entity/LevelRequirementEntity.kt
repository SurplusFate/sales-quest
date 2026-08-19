package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 等级晋级条件表 (v2)
 *
 * 升级到 level 级必须同时满足该级所有条件。
 * conditionType 取值: XP / TOTAL_MEET / TOTAL_QUERY / TOTAL_DEAL / STREAK_DAYS
 */
@Entity(tableName = "level_requirements")
data class LevelRequirementEntity(
    @PrimaryKey val id: String = "",
    val level: Int,
    val conditionType: String,
    val threshold: Int,
    val createdAt: Long = System.currentTimeMillis()
)
