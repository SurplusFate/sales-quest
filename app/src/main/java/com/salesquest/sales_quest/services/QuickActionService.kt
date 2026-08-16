package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil

/**
 * V1 快速操作服务
 *
 * 核心流程:
 * 1. 更新数据 (见人/查询/成交)
 * 2. 如果当天首次产生数据 → 锁定任务配置
 * 3. 刷新任务进度, 发放单个任务 XP
 * 4. 检查全部基础任务是否完成 → 触发连续作战 +1 + 奖励 XP
 * 5. 如果成交不参与基础任务 → 发放成交额外 XP
 * 6. 检查成就解锁
 */
class QuickActionService(
    private val db: AppDatabase,
    private val xpService: XpService,
    private val taskService: DailyTaskService,
    private val achievementService: AchievementService
) {

    suspend fun setPeopleSeen(count: Int) {
        xpService.setPeopleSeen(count)
        postUpdate()
    }

    suspend fun incrementQuery() {
        xpService.incrementQuery()
        postUpdate()
    }

    suspend fun setQuery(count: Int) {
        xpService.setQuery(count)
        postUpdate()
    }

    suspend fun incrementDeal() {
        xpService.incrementDeal()
        postUpdate()
    }

    suspend fun setDeal(count: Int) {
        xpService.setDeal(count)
        postUpdate()
    }

    /** 数据更新后的统一处理流程 */
    private suspend fun postUpdate() {
        try {
            // 1. 如果当天首次产生数据 → 锁定任务配置
            if (taskService.hasTodayData()) {
                taskService.lockTodayTasks()
            }

            // 2. 刷新任务进度, 发放单个任务 XP
            val newlyCompleted = taskService.refreshTodayProgress()
            for (task in newlyCompleted) {
                xpService.awardTaskXp(task.taskId, task.xpReward)
            }

            // 3. 检查全部基础任务是否完成
            if (taskService.checkAllTasksCompleted()) {
                xpService.onDailyTasksCompleted()
            }

            // 4. 如果成交不参与基础任务 → 发放成交额外 XP
            val config = taskService.getTodayConfig()
            if (!config.includeDeal) {
                val dateKey = DateUtil.dateKey()
                val dealCount = db.settingDao().getInt(SettingsKeys.deals(dateKey))
                if (dealCount > 0) {
                    xpService.awardDealExtraXp(dealCount)
                }
            }

            // 5. 检查成就解锁
            achievementService.checkAndUnlock()
        } catch (e: Exception) {
            AppLogger.error("QuickActionService", "postUpdate 失败: $e", e.stackTraceToString())
        }
    }
}
