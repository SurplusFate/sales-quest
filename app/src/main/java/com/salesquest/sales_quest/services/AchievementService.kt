package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.AppAchievements
import com.salesquest.sales_quest.core.AchievementDef
import com.salesquest.sales_quest.core.AchievementType
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.entity.AchievementEntity

/** 成就状态 (定义 + 解锁信息) */
data class AchievementStatus(
    val def: AchievementDef,
    val unlocked: Boolean,
    val unlockedAt: Long? = null
)

/**
 * V1.0.0 成就服务
 * 基于 Settings 中的累计数据检查成就
 */
class AchievementService(private val db: AppDatabase) {

    /** 检查并解锁所有可解锁成就, 返回本次新解锁的 id 列表 */
    suspend fun checkAndUnlock(): List<String> {
        val unlocked = mutableListOf<String>()
        for (def in AppAchievements.definitions) {
            val alreadyUnlocked = db.achievementDao().isUnlocked(def.id) > 0
            if (alreadyUnlocked) continue

            val shouldUnlock = checkCondition(def)
            if (shouldUnlock) {
                db.achievementDao().unlock(
                    AchievementEntity(
                        id = IdGenerator.gen("ach_"),
                        achievementId = def.id
                    )
                )
                unlocked.add(def.id)
            }
        }
        return unlocked
    }

    private suspend fun checkCondition(def: AchievementDef): Boolean {
        val dateKey = DateUtil.dateKey()
        return when (def.type) {
            AchievementType.FIRST_MEET,
            AchievementType.TOTAL_MEET -> db.settingDao().getInt(SettingsKeys.TOTAL_MEETS) >= def.target

            AchievementType.FIRST_QUERY,
            AchievementType.TOTAL_QUERY -> db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES) >= def.target

            AchievementType.FIRST_DEAL,
            AchievementType.TOTAL_DEAL -> db.settingDao().getInt(SettingsKeys.TOTAL_DEALS) >= def.target

            AchievementType.DAILY_QUERY -> db.settingDao().getInt(SettingsKeys.queries(dateKey)) >= def.target

            AchievementType.DAILY_DEAL -> db.settingDao().getInt(SettingsKeys.deals(dateKey)) >= def.target

            AchievementType.STREAK_DAYS -> {
                val stats = db.statsDao().getStats()
                stats?.streakDays ?: 0 >= def.target
            }
        }
    }

    /** 获取所有成就及其解锁状态 */
    suspend fun getAllStatuses(): List<AchievementStatus> {
        val unlocked = db.achievementDao().getAll()
        val unlockedById = unlocked.associateBy { it.achievementId }

        return AppAchievements.definitions.map { def ->
            val entity = unlockedById[def.id]
            AchievementStatus(
                def = def,
                unlocked = entity != null,
                unlockedAt = entity?.unlockedAt
            )
        }
    }
}
