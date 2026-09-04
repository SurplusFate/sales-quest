package com.salesquest.sales_quest.services

import androidx.room.withTransaction
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
 * V1.0.0 XP 服务
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
    private val levelService = LevelService(db)

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
            val newLevel = computeLevelWithConditions(newTotalXp)
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
            val newLevel = computeLevelWithConditions(newTotalXp)
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
            val newLevel = computeLevelWithConditions(newTotalXp)
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

    // ==================== 重置支持 ====================

    /**
     * 回滚今日已发放的全部 XP (供"清除今日数据"调用, 防止清空后重复刷 XP)
     *
     * 1. 删除今日全部 xp_records
     * 2. totalXp 扣减并重算等级 (基于最新累计指标)
     * 3. 若今日触发过连续作战奖励, 同步回退 streakDays / lastActiveDate
     *
     * 必须在 clearTodayData 已重算累计总计后调用; 返回是否发生回滚
     */
    suspend fun revokeTodayRewards(): Boolean {
        val start = DateUtil.dayStart()
        val end = start + 24L * 60 * 60 * 1000

        return db.withTransaction {
            val todayXp = db.xpDao().getXpToday(start, end)
            if (todayXp <= 0) return@withTransaction false

            val hadCompletion = db.xpDao().countActionTypeInRange("DAILY_COMPLETION", start, end) > 0
            db.xpDao().deleteByCreatedAtRange(start, end)

            val stats = getOrCreateStats()
            val newTotalXp = (stats.totalXp - todayXp).coerceAtLeast(0)

            // 连续作战回退: 今天完成的奖励撤销, 等效"上次活跃 = 昨天"
            var newStreak = stats.streakDays
            var newLastActive = stats.lastActiveDate
            if (hadCompletion) {
                newStreak = (newStreak - 1).coerceAtLeast(0)
                newLastActive = if (newStreak > 0) DateUtil.dayStart(DateUtil.yesterdayStart()) else null
            }

            val requirements = levelService.getRequirements()
            val totalMeet = db.settingDao().getInt(SettingsKeys.TOTAL_MEETS)
            val totalQuery = db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES)
            val totalDeal = db.settingDao().getInt(SettingsKeys.TOTAL_DEALS)
            val newLevel = LevelService.evaluateCurrentLevel(
                requirements = requirements,
                totalXp = newTotalXp,
                totalMeet = totalMeet,
                totalQuery = totalQuery,
                totalDeal = totalDeal,
                streakDays = newStreak
            )

            db.statsDao().updateStats(
                totalXp = newTotalXp,
                currentLevel = newLevel,
                streakDays = newStreak,
                lastActiveDate = newLastActive,
                updatedAt = System.currentTimeMillis()
            )
            AppLogger.info("XpService", "回滚今日 XP -$todayXp, totalXp=$newTotalXp, level=$newLevel, streak=$newStreak")
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

    /** 基于晋级条件计算当前等级 (XP + 累计指标) */
    private suspend fun computeLevelWithConditions(newTotalXp: Int): Int {
        val requirements = levelService.getRequirements()
        val totalMeet = db.settingDao().getInt(SettingsKeys.TOTAL_MEETS)
        val totalQuery = db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES)
        val totalDeal = db.settingDao().getInt(SettingsKeys.TOTAL_DEALS)
        val stats = getOrCreateStats()
        return LevelService.evaluateCurrentLevel(
            requirements = requirements,
            totalXp = newTotalXp,
            totalMeet = totalMeet,
            totalQuery = totalQuery,
            totalDeal = totalDeal,
            streakDays = stats.streakDays
        )
    }
}
