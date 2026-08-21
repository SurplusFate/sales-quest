package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil

/**
 * V1.0.0 快速操作服务
 *
 * 核心流程:
 * 1. 更新数据 (见人/查询/成交) + 校验销售漏斗 (成交 ≤ 查询 ≤ 见人)
 * 2. 刷新任务进度, 发放单个任务 XP (防重复)
 * 3. 检查全部基础任务是否完成 → 触发连续作战 +1 + 奖励 XP
 * 4. 如果成交不参与基础任务 → 发放成交额外 XP
 * 5. 检查成就解锁
 * 6. 标记自动备份 dirty
 *
 * 变更 (P1):
 * - 单指标更新时校验销售漏斗, 防止非法数据
 * - 不再锁定任务配置, 允许当天修改目标 (防重复奖励仍由 XpService 保证)
 */
class QuickActionService(
    private val db: AppDatabase,
    private val xpService: XpService,
    private val taskService: DailyTaskService,
    private val achievementService: AchievementService,
    private val onDataChanged: () -> Unit = {}
) {

    suspend fun setPeopleSeen(count: Int) {
        validateFunnel(count, getTodayQueries(), getTodayDeals())
        xpService.setPeopleSeen(count)
        postUpdate()
    }

    suspend fun incrementQuery() {
        val newCount = getTodayQueries() + 1
        validateFunnel(getTodayPeople(), newCount, getTodayDeals())
        xpService.incrementQuery()
        postUpdate()
    }

    suspend fun setQuery(count: Int) {
        validateFunnel(getTodayPeople(), count, getTodayDeals())
        xpService.setQuery(count)
        postUpdate()
    }

    suspend fun incrementDeal() {
        val newCount = getTodayDeals() + 1
        validateFunnel(getTodayPeople(), getTodayQueries(), newCount)
        xpService.incrementDeal()
        postUpdate()
    }

    suspend fun setDeal(count: Int) {
        validateFunnel(getTodayPeople(), getTodayQueries(), count)
        xpService.setDeal(count)
        postUpdate()
    }

    /** 读取今日见人数 */
    private suspend fun getTodayPeople(): Int =
        db.settingDao().getInt(SettingsKeys.peopleSeen(DateUtil.dateKey()))

    /** 读取今日查询数 */
    private suspend fun getTodayQueries(): Int =
        db.settingDao().getInt(SettingsKeys.queries(DateUtil.dateKey()))

    /** 读取今日成交数 */
    private suspend fun getTodayDeals(): Int =
        db.settingDao().getInt(SettingsKeys.deals(DateUtil.dateKey()))

    /**
     * 销售漏斗校验: 0 <= 成交 <= 查询 <= 见人
     * @throws IllegalArgumentException 校验失败时抛出, 消息为中文提示
     */
    private fun validateFunnel(peopleSeen: Int, queries: Int, deals: Int) {
        if (peopleSeen < 0 || queries < 0 || deals < 0) {
            throw IllegalArgumentException("数字不能为负数")
        }
        if (queries > peopleSeen) {
            throw IllegalArgumentException("查询数不能大于见人数")
        }
        if (deals > queries) {
            throw IllegalArgumentException("成交数不能大于查询数")
        }
    }

    /**
     * 数据更新后的统一处理流程
     *
     * 错误处理规则:
     * - 半核心操作 (任务进度 / XP 发放): 失败记日志, 不阻断后续半核心操作
     * - 附加操作 (成就解锁): 失败记日志, 不影响任何其他操作
     * - 核心数据 (见人/查询/成交数) 已在调用方写入, 此处不处理
     */
    private suspend fun postUpdate() {
        // 半核心: 任务进度刷新 + XP 发放
        try {
            val newlyCompleted = taskService.refreshTodayProgress()
            for (task in newlyCompleted) {
                xpService.awardTaskXp(task.taskId, task.xpReward)
            }

            if (taskService.checkAllTasksCompleted()) {
                xpService.onDailyTasksCompleted()
            }

            val config = taskService.getTodayConfig()
            if (!config.includeDeal) {
                val dateKey = DateUtil.dateKey()
                val dealCount = db.settingDao().getInt(SettingsKeys.deals(dateKey))
                if (dealCount > 0) {
                    xpService.awardDealExtraXp(dealCount)
                }
            }
        } catch (e: Exception) {
            AppLogger.error("QuickActionService", "任务/XP 处理失败: $e", e.stackTraceToString())
        }

        // 附加: 成就解锁 (失败不影响核心数据和 XP)
        try {
            achievementService.checkAndUnlock()
        } catch (e: Exception) {
            AppLogger.error("QuickActionService", "成就检查失败: $e", e.stackTraceToString())
        }

        // 无论上述是否异常, 数据已写入, 标记自动备份
        onDataChanged()
    }
}
