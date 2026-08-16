package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 设置表 (KV 存储)
 */
@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
