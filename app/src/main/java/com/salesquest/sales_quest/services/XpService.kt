package com.salesquest.sales_quest.services

import androidx.room.withTransaction
import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.core.XpRewards
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.data.entity.UserStatEntity
import com.salesquest.sales_quest.data.entity.XpRecordEntity
import kotlinx.coroutines.flow.firstOrNull

/**
 * V1.0 XP 服务
 *
 * 核心原则: XP 从销售实际行为产生, 不增加额外记录负担
 *
 * 核心变更:
 * 1. 连续作战仅在全部基础任务完成时 +1 (不再每次操作就 +1)
 * 2. 完成全部基础任务 → 发放额外 XP + 连续作战 +1
 * 3. 成交不参与基础任务时 → 每次成交发放额外 XP
 */
class XpService(private val db: AppDatabase) {

    private val dailyStatsService = DailyStatsService(db)

    // ==================== 数据操作 (不再更新 streak) ====================

    /** 设置今日见人数 (委托 DailyStatsService 统一累计逻辑) */
    suspend fun setPeopleSeen(count: Int) {
        dailyStatsService.updateDailyMetric(DateUtil.dateKey(), "MEET", count)
    }

    /** 查询 +1 */
    suspend fun incrementQuery() {
        val dateKey = DateUtil.dateKey()
        val current = db.settingDao().getInt(SettingsKeys.queries(dateKey))
        dailyStatsService.updateDailyMetric(dateKey, "QUERY", current + 1)
    }

    /** 设置今日查询数 (直接输入) */
    suspend fun setQuery(count: Int) {
        dailyStatsService.updateDailyMetric(DateUtil.dateKey(), "QUERY", count)
    }

    /** 成交 +1 */
    suspend fun incrementDeal() {
        val dateKey = DateUtil.dateKey()
        val current = db.settingDao().getInt(SettingsKeys.deals(dateKey))
        dailyStatsService.updateDailyMetric(dateKey, "DEAL", current + 1)
    }

    /** 设置今日成交数 (直接输入) */
    suspend fun setDeal(count: Int) {
        dailyStatsService.updateDailyMetric(DateUtil.dateKey(), "DEAL", count)
    }

    // ==================== XP 发放 ====================

    /** 发放任务完成 XP (单个任务), 同一任务同一天只能发放一次 */
    suspend fun awardTaskXp(taskId: String, xpAmount: Int): Int {
        val dateKey = DateUtil.dateKey()
        val xpKey = SettingsKeys.taskXp(taskId, dateKey)

        return db.withTransaction {
            val alreadyAwarded = db.settingDao().get(xpKey)
            if (alreadyAwarded != null) return@withTransaction 0

            db.xpDao().insertXp(
                XpRecordEntity(
                    id = IdGenerator.gen("xp_"),
                    customerId = "daily",
                    actionType = "TASK_$taskId",
                    xp = xpAmount
                )
            )
            db.settingDao().set(SettingEntity(key = xpKey, value = "1"))

            val stats = getOrCreateStats()
            val newTotalXp = stats.totalXp + xpAmount
            val newLevel = AppLevels.getLevel(newTotalXp).level
            db.statsDao().updateStats(
                totalXp = newTotalXp,
                currentLevel = newLevel,
                streakDays = stats.streakDays,
                lastActiveDate = stats.lastActiveDate,
                updatedAt = System.currentTimeMillis()
            )
            AppLogger.info("XpService", "任务 $taskId 完成, +$xpAmount XP")
            xpAmount
        }
    }

    /** 发放成交额外 XP (当成交不参与基础任务时) */
    suspend fun awardDealExtraXp(dealCount: Int): Int {
        val dateKey = DateUtil.dateKey()
        val awardedKey = SettingsKeys.dealExtraXpAwarded(dateKey)

        return db.withTransaction {
            val alreadyAwardedCount = db.settingDao().getInt(awardedKey)
            val newDeals = dealCount - alreadyAwardedCount
            if (newDeals <= 0) return@withTransaction 0

            val totalXp = newDeals * XpRewards.dealExtraXp
            db.xpDao().insertXp(
                XpRecordEntity(
                    id = IdGenerator.gen("xp_"),
                    customerId = "daily",
                    actionType = "DEAL_EXTRA",
                    xp = totalXp
                )
            )
            db.settingDao().setInt(awardedKey, dealCount)

            val stats = getOrCreateStats()
            val newTotalXp = stats.totalXp + totalXp
            val newLevel = AppLevels.getLevel(newTotalXp).level
            db.statsDao().updateStats(
                totalXp = newTotalXp,
                currentLevel = newLevel,
                streakDays = stats.streakDays,
                lastActiveDate = stats.lastActiveDate,
                updatedAt = System.currentTimeMillis()
            )
            AppLogger.info("XpService", "成交额外 XP +$totalXp ($newDeals 单)")
            totalXp
        }
    }

    // ==================== 连续作战 (仅在全部基础任务完成时触发) ====================

    /** 今日全部基础任务完成时的处理 */
    suspend fun onDailyTasksCompleted(): Boolean {
        val dateKey = DateUtil.dateKey()
        val completionKey = SettingsKeys.dailyCompletion(dateKey)

        return db.withTransaction {
            val alreadyTriggered = db.settingDao().get(completionKey)
            if (alreadyTriggered != null) return@withTransaction false

            db.xpDao().insertXp(
                XpRecordEntity(
                    id = IdGenerator.gen("xp_"),
                    customerId = "daily",
                    actionType = "DAILY_COMPLETION",
                    xp = XpRewards.dailyCompletionBonus
                )
            )

            val stats = getOrCreateStats()
            val today = DateUtil.dayStart()
            val lastActive = stats.lastActiveDate?.let { DateUtil.dayStart(it) }

            val newStreakDays = when {
                lastActive == null -> 1
                lastActive == today -> if (stats.streakDays > 0) stats.streakDays else 1
                lastActive == today - 24L * 60 * 60 * 1000 -> stats.streakDays + 1
                else -> 1
            }

            val newTotalXp = stats.totalXp + XpRewards.dailyCompletionBonus
            val newLevel = AppLevels.getLevel(newTotalXp).level
            db.statsDao().updateStats(
                totalXp = newTotalXp,
                currentLevel = newLevel,
                streakDays = newStreakDays,
                lastActiveDate = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.settingDao().set(SettingEntity(key = completionKey, value = "1"))

            AppLogger.info("XpService", "今日作战完成! +${XpRewards.dailyCompletionBonus} XP, 连续作战 $newStreakDays 天")
            true
        }
    }

    private suspend fun getOrCreateStats(): UserStatEntity {
        val existing = db.statsDao().getStats()
        if (existing != null) return existing
        val stats = UserStatEntity(id = "default")
        db.statsDao().insertStats(stats)
        return stats
    }
}
