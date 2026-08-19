package com.salesquest.sales_quest.services

import androidx.room.withTransaction
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.entity.AchievementEntity
import com.salesquest.sales_quest.data.entity.CustomerEntity
import com.salesquest.sales_quest.data.entity.CustomerEventEntity
import com.salesquest.sales_quest.data.entity.DailySummaryEntity
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.data.entity.FollowUpEntity
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.data.entity.UserStatEntity
import com.salesquest.sales_quest.data.entity.XpRecordEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** 备份校验结果 */
sealed class BackupValidationResult {
    data class Success(val data: BackupData) : BackupValidationResult()
    data class Error(val message: String) : BackupValidationResult()
}

/**
 * 备份服务 - 数据级导出/恢复
 *
 * 备份内容: 全部业务表 (设置/客户/事件/XP/跟进/任务/统计/成就/晋级条件/总结)
 * 备份文件: zip (内含 data.json + 可选数据库文件 + meta.json)
 * 恢复原则: 清空当前数据后按备份数据重建, 不依赖替换数据库文件 (跨版本安全)
 */
class BackupService(private val db: AppDatabase) {

    // ==================== 导出 ====================

    /** 从数据库导出全部数据 */
    suspend fun exportBackupData(): BackupData {
        return BackupData(
            settings = db.settingDao().getAll().map { BackupSetting(it.key, it.value) },
            customers = db.customerDao().getAll().map { it.toBackup() },
            customerEvents = db.eventDao().getAll().map { it.toBackup() },
            xpRecords = db.xpDao().getAll().map { it.toBackup() },
            followUps = db.followUpDao().getAll().map { it.toBackup() },
            dailyTasks = db.taskDao().getAll().map { it.toBackup() },
            userStats = db.statsDao().getStats()?.let { listOf(it.toBackup()) } ?: emptyList(),
            achievements = db.achievementDao().getAll().map { it.toBackup() },
            levelRequirements = db.levelRequirementDao().getAll().map { it.toBackup() },
            dailySummaries = db.dailySummaryDao().getAll().map { it.toBackup() }
        )
    }

    /** 打包 zip 字节流 (data.json + meta.json + 可选数据库文件) */
    fun createBackupZip(data: BackupData, dbFileBytes: ByteArray? = null): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry(BackupEntryNames.DATA_JSON))
            zos.write(BackupJson.encodeData(data).toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry(BackupEntryNames.META_JSON))
            zos.write(BackupJson.encodeMeta(BackupMeta(createdAt = data.createdAt)).toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            if (dbFileBytes != null) {
                zos.putNextEntry(ZipEntry(BackupEntryNames.DB_FILE))
                zos.write(dbFileBytes)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    // ==================== 解析/校验 ====================

    /** 解析 zip 并校验备份有效性 */
    fun parseBackupZip(bytes: ByteArray): BackupValidationResult {
        val dataJson = try {
            extractEntry(bytes, BackupEntryNames.DATA_JSON)
        } catch (e: Exception) {
            return BackupValidationResult.Error("备份文件无法解析: ${e.message}")
        } ?: return BackupValidationResult.Error("备份文件缺少 data.json")

        return try {
            val data = BackupJson.decodeData(dataJson)
            validate(data)
        } catch (e: Exception) {
            BackupValidationResult.Error("备份数据格式错误: ${e.message}")
        }
    }

    /** 校验备份数据 (含销售漏斗约束) */
    fun validate(data: BackupData): BackupValidationResult {
        if (data.formatVersion != 1) {
            return BackupValidationResult.Error("不支持的备份版本: ${data.formatVersion}")
        }
        for (s in data.settings) {
            if (s.key.isBlank()) return BackupValidationResult.Error("备份设置项缺失 key")
        }

        // 销售漏斗校验: 每个日期的 成交 <= 查询 <= 见人
        val funnelErrors = validateBackupFunnel(data.settings)
        if (funnelErrors.isNotEmpty()) {
            return BackupValidationResult.Error("备份数据违反销售漏斗约束:\n${funnelErrors.joinToString("\n")}")
        }

        return BackupValidationResult.Success(data)
    }

    /** 遍历备份数据中的每日指标, 校验 0 <= 成交 <= 查询 <= 见人 */
    private fun validateBackupFunnel(settings: List<BackupSetting>): List<String> {
        val map = settings.associate { it.key to it.value }
        val errors = mutableListOf<String>()

        // 收集所有出现过的日期 key
        val dateKeys = mutableSetOf<String>()
        for (s in settings) {
            when {
                s.key.startsWith("people_seen_") -> dateKeys.add(s.key.removePrefix("people_seen_"))
                s.key.startsWith("queries_") -> dateKeys.add(s.key.removePrefix("queries_"))
                s.key.startsWith("deals_") -> dateKeys.add(s.key.removePrefix("deals_"))
            }
        }

        for (dateKey in dateKeys) {
            val meet = map["people_seen_$dateKey"]?.toIntOrNull() ?: 0
            val query = map["queries_$dateKey"]?.toIntOrNull() ?: 0
            val deal = map["deals_$dateKey"]?.toIntOrNull() ?: 0
            if (query > meet) {
                errors.add("$dateKey: 查询数($query) > 见人数($meet)")
            }
            if (deal > query) {
                errors.add("$dateKey: 成交数($deal) > 查询数($query)")
            }
        }
        return errors
    }

    private fun extractEntry(bytes: ByteArray, entryName: String): String? {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == entryName) {
                    return zis.readBytes().toString(Charsets.UTF_8)
                }
                entry = zis.nextEntry
            }
        }
        return null
    }

    // ==================== 恢复 ====================

    /** 恢复备份数据 (清空当前数据后重建)。返回恢复的条目统计 */
    suspend fun restoreBackupData(data: BackupData): RestoreStats {
        if (data.formatVersion != 1) throw IllegalArgumentException("不支持的备份版本: ${data.formatVersion}")

        db.withTransaction {
            db.settingDao().clearAll()
            db.customerDao().clearAll()
            db.eventDao().clearAll()
            db.xpDao().clearAll()
            db.followUpDao().clearAll()
            db.taskDao().clearAll()
            db.achievementDao().clearAll()
            db.levelRequirementDao().clearAll()
            db.dailySummaryDao().clearAll()

            data.settings.forEach { db.settingDao().set(SettingEntity(it.key, it.value)) }
            data.customers.forEach { db.customerDao().insertCustomer(it.toEntity()) }
            data.customerEvents.forEach { db.eventDao().insertEvent(it.toEntity()) }
            data.xpRecords.forEach { db.xpDao().insertXp(it.toEntity()) }
            data.followUps.forEach { db.followUpDao().insertFollowUp(it.toEntity()) }
            data.dailyTasks.forEach { db.taskDao().upsertTask(it.toEntity()) }
            data.userStats.forEach { db.statsDao().insertStats(it.toEntity()) }
            data.achievements.forEach { db.achievementDao().unlock(it.toEntity()) }
            data.levelRequirements.forEach { db.levelRequirementDao().insert(it.toEntity()) }
            data.dailySummaries.forEach { db.dailySummaryDao().upsert(it.toEntity()) }
        }

        return RestoreStats(
            customers = data.customers.size,
            settings = data.settings.size,
            xpRecords = data.xpRecords.size,
            summaries = data.dailySummaries.size
        )
    }

    /** 当前数据库文件字节 (尽力而为; 内存库返回 null) */
    fun readDatabaseFileBytes(): ByteArray? {
        return try {
            val path = db.openHelper.writableDatabase.path ?: return null
            val file = java.io.File(path)
            if (file.exists()) file.readBytes() else null
        } catch (e: Exception) {
            null
        }
    }
}

/** 恢复统计 */
data class RestoreStats(
    val customers: Int = 0,
    val settings: Int = 0,
    val xpRecords: Int = 0,
    val summaries: Int = 0
)

// ==================== 实体映射 ====================

private fun CustomerEntity.toBackup() = BackupCustomer(
    id = id, name = name, phone = phone, operator = operator,
    selfReportedCost = selfReportedCost, actualCost = actualCost,
    packageName = packageName, traffic = traffic, minutes = minutes,
    broadband = broadband, subCards = subCards, camera = camera,
    contractStatus = contractStatus, otherBusiness = otherBusiness, status = status,
    valueScore = valueScore, valueLevel = valueLevel, salesStage = salesStage,
    nextAction = nextAction, nextFollowUpAt = nextFollowUpAt, note = note,
    createdAt = createdAt, updatedAt = updatedAt
)

private fun CustomerEventEntity.toBackup() = BackupCustomerEvent(
    id = id, customerId = customerId, eventType = eventType,
    eventTime = eventTime, note = note, metadata = metadata
)

private fun XpRecordEntity.toBackup() = BackupXpRecord(
    id = id, customerId = customerId, actionType = actionType,
    xp = xp, createdAt = createdAt
)

private fun FollowUpEntity.toBackup() = BackupFollowUp(
    id = id, customerId = customerId, scheduledAt = scheduledAt,
    content = content, completed = completed, completedAt = completedAt, createdAt = createdAt
)

private fun DailyTaskEntity.toBackup() = BackupDailyTask(
    id = id, date = date, taskId = taskId, tier = tier, metric = metric,
    target = target, progress = progress, completed = completed,
    xpReward = xpReward, createdAt = createdAt
)

private fun UserStatEntity.toBackup() = BackupUserStat(
    id = id, totalXp = totalXp, currentLevel = currentLevel,
    streakDays = streakDays, lastActiveDate = lastActiveDate, updatedAt = updatedAt
)

private fun AchievementEntity.toBackup() = BackupAchievement(
    id = id, achievementId = achievementId, unlockedAt = unlockedAt
)

private fun LevelRequirementEntity.toBackup() = BackupLevelRequirement(
    id = id, level = level, conditionType = conditionType,
    threshold = threshold, createdAt = createdAt
)

private fun DailySummaryEntity.toBackup() = BackupDailySummary(
    dateKey = dateKey, good = good, problems = problems,
    customerFeedback = customerFeedback, discovery = discovery,
    improvement = improvement, updatedAt = updatedAt
)

// ==================== DTO → 实体 ====================

private fun BackupCustomer.toEntity() = CustomerEntity(
    id = id, name = name, phone = phone, operator = operator,
    selfReportedCost = selfReportedCost, actualCost = actualCost,
    packageName = packageName, traffic = traffic, minutes = minutes,
    broadband = broadband, subCards = subCards, camera = camera,
    contractStatus = contractStatus, otherBusiness = otherBusiness, status = status,
    valueScore = valueScore, valueLevel = valueLevel, salesStage = salesStage,
    nextAction = nextAction, nextFollowUpAt = nextFollowUpAt, note = note,
    createdAt = createdAt, updatedAt = updatedAt
)

private fun BackupCustomerEvent.toEntity() = CustomerEventEntity(
    id = id, customerId = customerId, eventType = eventType,
    eventTime = eventTime, note = note, metadata = metadata
)

private fun BackupXpRecord.toEntity() = XpRecordEntity(
    id = id, customerId = customerId, actionType = actionType,
    xp = xp, createdAt = createdAt
)

private fun BackupFollowUp.toEntity() = FollowUpEntity(
    id = id, customerId = customerId, scheduledAt = scheduledAt,
    content = content, completed = completed, completedAt = completedAt, createdAt = createdAt
)

private fun BackupDailyTask.toEntity() = DailyTaskEntity(
    id = id, date = date, taskId = taskId, tier = tier, metric = metric,
    target = target, progress = progress, completed = completed,
    xpReward = xpReward, createdAt = createdAt
)

private fun BackupUserStat.toEntity() = UserStatEntity(
    id = id, totalXp = totalXp, currentLevel = currentLevel,
    streakDays = streakDays, lastActiveDate = lastActiveDate, updatedAt = updatedAt
)

private fun BackupAchievement.toEntity() = AchievementEntity(
    id = id, achievementId = achievementId, unlockedAt = unlockedAt
)

private fun BackupLevelRequirement.toEntity() = LevelRequirementEntity(
    id = id, level = level, conditionType = conditionType,
    threshold = threshold, createdAt = createdAt
)

private fun BackupDailySummary.toEntity() = DailySummaryEntity(
    dateKey = dateKey, good = good, problems = problems,
    customerFeedback = customerFeedback, discovery = discovery,
    improvement = improvement, updatedAt = updatedAt
)
