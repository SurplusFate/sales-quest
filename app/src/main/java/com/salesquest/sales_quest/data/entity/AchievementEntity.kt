package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 成就解锁表
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String = "",
    val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
