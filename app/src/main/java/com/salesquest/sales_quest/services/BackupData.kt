package com.salesquest.sales_quest.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 云备份数据结构 - 覆盖全部业务表, 用于数据级备份/恢复
 * 以 JSON 形式打包进 zip, 跨版本兼容且不依赖替换数据库文件
 */
@Serializable
data class BackupData(
    val formatVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val settings: List<BackupSetting> = emptyList(),
    val customers: List<BackupCustomer> = emptyList(),
    val customerEvents: List<BackupCustomerEvent> = emptyList(),
    val xpRecords: List<BackupXpRecord> = emptyList(),
    val followUps: List<BackupFollowUp> = emptyList(),
    val dailyTasks: List<BackupDailyTask> = emptyList(),
    val userStats: List<BackupUserStat> = emptyList(),
    val achievements: List<BackupAchievement> = emptyList(),
    val levelRequirements: List<BackupLevelRequirement> = emptyList(),
    val dailySummaries: List<BackupDailySummary> = emptyList(),
    val executionRecords: List<BackupExecutionRecord> = emptyList()
)

@Serializable
data class BackupSetting(val key: String, val value: String)

@Serializable
data class BackupCustomer(
    val id: String,
    val name: String,
    val phone: String,
    val operator: String,
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
    val status: String,
    val valueScore: Int = 0,
    val valueLevel: String,
    val salesStage: String,
    val nextAction: String? = null,
    val nextFollowUpAt: Long? = null,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BackupCustomerEvent(
    val id: String,
    val customerId: String,
    val eventType: String,
    val eventTime: Long,
    val note: String? = null,
    val metadata: String? = null
)

@Serializable
data class BackupXpRecord(
    val id: String,
    val customerId: String? = null,
    val actionType: String,
    val xp: Int,
    val createdAt: Long
)

@Serializable
data class BackupFollowUp(
    val id: String,
    val customerId: String,
    val scheduledAt: Long,
    val content: String? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long
)

@Serializable
data class BackupDailyTask(
    val id: String,
    val date: String,
    val taskId: String,
    val tier: String,
    val metric: String,
    val target: Int,
    val progress: Int = 0,
    val completed: Boolean = false,
    val xpReward: Int,
    val createdAt: Long
)

@Serializable
data class BackupUserStat(
    val id: String,
    val totalXp: Int,
    val currentLevel: Int,
    val streakDays: Int,
    val lastActiveDate: Long? = null,
    val updatedAt: Long
)

@Serializable
data class BackupAchievement(
    val id: String,
    val achievementId: String,
    val unlockedAt: Long
)

@Serializable
data class BackupLevelRequirement(
    val id: String,
    val level: Int,
    val conditionType: String,
    val threshold: Int,
    val createdAt: Long
)

@Serializable
data class BackupDailySummary(
    val dateKey: String,
    val good: String,
    val problems: String,
    val customerFeedback: String,
    val discovery: String,
    val improvement: String,
    val updatedAt: Long
)

@Serializable
data class BackupExecutionRecord(
    val id: String,
    val dateKey: String,
    val recordTime: Long? = null,
    val timePrecision: String,
    val periodLabel: String? = null,
    val peopleSeen: Int,
    val queries: Int,
    val deals: Int,
    val createdAt: Long,
    val updatedAt: Long
)

/** 备份 zip 内文件条目名 */
object BackupEntryNames {
    const val DATA_JSON = "data.json"
    const val DB_FILE = "sales_quest.db"
    const val META_JSON = "meta.json"
}

@Serializable
data class BackupMeta(
    val formatVersion: Int = 1,
    val appVersion: String = "1.0",
    val createdAt: Long = System.currentTimeMillis()
)

/** JSON 编解码工具 (供 BackupService/WebDav 层复用) */
object BackupJson {
    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encodeData(data: BackupData): String = json.encodeToString(BackupData.serializer(), data)
    fun decodeData(raw: String): BackupData = json.decodeFromString(BackupData.serializer(), raw)
    fun encodeMeta(meta: BackupMeta): String = json.encodeToString(BackupMeta.serializer(), meta)
    fun decodeMeta(raw: String): BackupMeta = json.decodeFromString(BackupMeta.serializer(), raw)
}
