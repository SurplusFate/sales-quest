package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.AppTasks
import com.salesquest.sales_quest.core.DefaultTaskConfig
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.data.entity.SettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 每日任务配置 (用户自定义) */
data class DailyTaskConfig(
    val meetTarget: Int = DefaultTaskConfig.recommendedMeetTarget,
    val queryTarget: Int = DefaultTaskConfig.recommendedQueryTarget,
    val dealTarget: Int = DefaultTaskConfig.recommendedDealTarget,
    val includeMeet: Boolean = DefaultTaskConfig.recommendedIncludeMeet,
    val includeQuery: Boolean = DefaultTaskConfig.recommendedIncludeQuery,
    val includeDeal: Boolean = DefaultTaskConfig.recommendedIncludeDeal,
    val locked: Boolean = false,
    val allCompleted: Boolean = false
) {
    val hasAnyIncluded: Boolean get() = includeMeet || includeQuery || includeDeal

    val includedMetrics: List<String> get() {
        val result = mutableListOf<String>()
        if (includeMeet) result.add("MEET")
        if (includeQuery) result.add("QUERY")
        if (includeDeal) result.add("DEAL")
        return result
    }

    fun getTarget(metricCode: String): Int = when (metricCode) {
        "MEET" -> meetTarget
        "QUERY" -> queryTarget
        "DEAL" -> dealTarget
        else -> 0
    }

    fun isIncluded(metricCode: String): Boolean = when (metricCode) {
        "MEET" -> includeMeet
        "QUERY" -> includeQuery
        "DEAL" -> includeDeal
        else -> false
    }

    fun copyConfig(
        meetTarget: Int = this.meetTarget,
        queryTarget: Int = this.queryTarget,
        dealTarget: Int = this.dealTarget,
        includeMeet: Boolean = this.includeMeet,
        includeQuery: Boolean = this.includeQuery,
        includeDeal: Boolean = this.includeDeal,
        locked: Boolean = this.locked,
        allCompleted: Boolean = this.allCompleted
    ): DailyTaskConfig = DailyTaskConfig(
        meetTarget, queryTarget, dealTarget, includeMeet, includeQuery, includeDeal, locked, allCompleted
    )
}

/**
 * V1.0.0 每日任务服务
 *
 * 核心变更:
 * 1. 任务目标由用户自定义, 不再硬编码
 * 2. 成交默认不参与基础任务, 用户可自行开启
 * 3. 当天产生数据后任务目标锁定, 不可修改
 * 4. 连续作战仅在全部基础任务完成时 +1
 */
class DailyTaskService(private val db: AppDatabase) {

    // ==================== 默认配置 (用户偏好) ====================

    /** 获取用户默认任务配置 (用于新一天的默认值) */
    suspend fun getDefaultConfig(): DailyTaskConfig {
        val meetTarget = readIntWithDefault(SettingsKeys.DEFAULT_MEET_TARGET, DefaultTaskConfig.recommendedMeetTarget)
        val queryTarget = readIntWithDefault(SettingsKeys.DEFAULT_QUERY_TARGET, DefaultTaskConfig.recommendedQueryTarget)
        val dealTarget = readIntWithDefault(SettingsKeys.DEFAULT_DEAL_TARGET, DefaultTaskConfig.recommendedDealTarget)
        val includeMeet = readBoolWithDefault(SettingsKeys.DEFAULT_INCLUDE_MEET, DefaultTaskConfig.recommendedIncludeMeet)
        val includeQuery = readBoolWithDefault(SettingsKeys.DEFAULT_INCLUDE_QUERY, DefaultTaskConfig.recommendedIncludeQuery)
        val includeDeal = readBoolWithDefault(SettingsKeys.DEFAULT_INCLUDE_DEAL, DefaultTaskConfig.recommendedIncludeDeal)
        return DailyTaskConfig(meetTarget, queryTarget, dealTarget, includeMeet, includeQuery, includeDeal)
    }

    private suspend fun readIntWithDefault(key: String, default: Int): Int {
        val raw = db.settingDao().get(key) ?: return default
        return raw.toIntOrNull() ?: default
    }

    private suspend fun readBoolWithDefault(key: String, default: Boolean): Boolean {
        val raw = db.settingDao().get(key) ?: return default
        return raw == "1"
    }

    /** 保存用户默认任务配置 */
    suspend fun saveDefaultConfig(config: DailyTaskConfig) {
        db.settingDao().setInt(SettingsKeys.DEFAULT_MEET_TARGET, config.meetTarget)
        db.settingDao().setInt(SettingsKeys.DEFAULT_QUERY_TARGET, config.queryTarget)
        db.settingDao().setInt(SettingsKeys.DEFAULT_DEAL_TARGET, config.dealTarget)
        db.settingDao().setInt(SettingsKeys.DEFAULT_INCLUDE_MEET, if (config.includeMeet) 1 else 0)
        db.settingDao().setInt(SettingsKeys.DEFAULT_INCLUDE_QUERY, if (config.includeQuery) 1 else 0)
        db.settingDao().setInt(SettingsKeys.DEFAULT_INCLUDE_DEAL, if (config.includeDeal) 1 else 0)
    }

    // ==================== 每日配置 ====================

    /** 获取某天的任务配置, 如果当天没有配置, 返回默认配置 (未锁定) */
    suspend fun getDayConfig(time: Long = System.currentTimeMillis()): DailyTaskConfig {
        val dateKey = DateUtil.dateKey(time)
        val hasConfig = db.settingDao().get(SettingsKeys.taskConfig(dateKey, "meet_target"))
        if (hasConfig == null) {
            val defaultConfig = getDefaultConfig()
            return defaultConfig.copyConfig(locked = false, allCompleted = false)
        }
        return buildConfigFromSettings(db.settingDao().getAll(), dateKey)
    }

    /**
     * 响应式监听今天的任务配置。
     *
     * 配置修改 → settings 表写入 → watchAll 发出新值 → 首页自动重组。
     * 首页直接使用本 Flow, 保证「显示目标 = 今日实际生效配置」始终一致。
     */
    fun watchTodayConfig(): Flow<DailyTaskConfig> {
        return db.settingDao().watchAll().map { settings ->
            buildConfigFromSettings(settings, DateUtil.dateKey())
        }
    }

    /** 由 settings 快照构建某天配置 (供响应式数据源复用, 与 getDayConfig 语义一致) */
    private fun buildConfigFromSettings(
        settings: List<SettingEntity>,
        dateKey: String
    ): DailyTaskConfig {
        val map = settings.associate { it.key to it.value }
        val hasConfig = map[SettingsKeys.taskConfig(dateKey, "meet_target")] != null
        if (!hasConfig) {
            return DailyTaskConfig(
                meetTarget = map[SettingsKeys.DEFAULT_MEET_TARGET]?.toIntOrNull()
                    ?: DefaultTaskConfig.recommendedMeetTarget,
                queryTarget = map[SettingsKeys.DEFAULT_QUERY_TARGET]?.toIntOrNull()
                    ?: DefaultTaskConfig.recommendedQueryTarget,
                dealTarget = map[SettingsKeys.DEFAULT_DEAL_TARGET]?.toIntOrNull()
                    ?: DefaultTaskConfig.recommendedDealTarget,
                includeMeet = map[SettingsKeys.DEFAULT_INCLUDE_MEET] == "1",
                includeQuery = map[SettingsKeys.DEFAULT_INCLUDE_QUERY] == "1",
                includeDeal = map[SettingsKeys.DEFAULT_INCLUDE_DEAL] == "1",
                locked = false,
                allCompleted = false
            )
        }
        return DailyTaskConfig(
            meetTarget = map[SettingsKeys.taskConfig(dateKey, "meet_target")]?.toIntOrNull() ?: 0,
            queryTarget = map[SettingsKeys.taskConfig(dateKey, "query_target")]?.toIntOrNull() ?: 0,
            dealTarget = map[SettingsKeys.taskConfig(dateKey, "deal_target")]?.toIntOrNull() ?: 0,
            includeMeet = map[SettingsKeys.taskConfig(dateKey, "include_meet")] == "1",
            includeQuery = map[SettingsKeys.taskConfig(dateKey, "include_query")] == "1",
            includeDeal = map[SettingsKeys.taskConfig(dateKey, "include_deal")] == "1",
            locked = map[SettingsKeys.taskConfig(dateKey, "locked")] == "1",
            allCompleted = map[SettingsKeys.taskConfig(dateKey, "all_completed")] == "1"
        )
    }

    /** 设置某天的任务配置 (允许当天修改, 不再锁定) */
    suspend fun setDayConfig(time: Long, config: DailyTaskConfig) {
        val dateKey = DateUtil.dateKey(time)

        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "meet_target"), config.meetTarget)
        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "query_target"), config.queryTarget)
        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "deal_target"), config.dealTarget)
        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "include_meet"), if (config.includeMeet) 1 else 0)
        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "include_query"), if (config.includeQuery) 1 else 0)
        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "include_deal"), if (config.includeDeal) 1 else 0)

        saveDefaultConfig(config)
        rebuildDayTasks(time, config)
    }

    /** 不再锁定任务 (P1 变更: 允许当天修改目标, 防重复奖励由 XpService 保证) */
    suspend fun lockTodayTasks() {
        val dateKey = DateUtil.dateKey()
        val hasConfig = db.settingDao().get(SettingsKeys.taskConfig(dateKey, "meet_target"))
        if (hasConfig == null) {
            val defaultConfig = getDefaultConfig()
            db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "meet_target"), defaultConfig.meetTarget)
            db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "query_target"), defaultConfig.queryTarget)
            db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "deal_target"), defaultConfig.dealTarget)
            db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "include_meet"), if (defaultConfig.includeMeet) 1 else 0)
            db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "include_query"), if (defaultConfig.includeQuery) 1 else 0)
            db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "include_deal"), if (defaultConfig.includeDeal) 1 else 0)
        }
        // 不再写入 locked=1
    }

    /** 检查当天任务是否已锁定 (P1: 始终返回 false, 允许修改) */
    suspend fun isTodayLocked(): Boolean = false

    /** 检查当天是否有数据产生 */
    suspend fun hasTodayData(): Boolean {
        val dateKey = DateUtil.dateKey()
        val meet = db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey))
        val query = db.settingDao().getInt(SettingsKeys.queries(dateKey))
        val deal = db.settingDao().getInt(SettingsKeys.deals(dateKey))
        return meet > 0 || query > 0 || deal > 0
    }

    // ==================== 任务行管理 ====================

    /** 确保当天任务已创建 (使用当天配置) */
    suspend fun ensureTodayTasks() {
        val now = System.currentTimeMillis()
        val today = DateUtil.dateKey(now)
        val existing = db.taskDao().getByDate(today)
        if (existing.isEmpty()) {
            val todayConfigExists = db.settingDao().get(SettingsKeys.taskConfig(today, "meet_target"))
            if (todayConfigExists == null) {
                val inherited = inheritYesterdayConfig()
                if (!inherited) {
                    val config = getTodayConfig()
                    rebuildDayTasks(now, config)
                }
            } else {
                val config = getTodayConfig()
                rebuildDayTasks(now, config)
            }
        }
    }

    suspend fun getTodayConfig(): DailyTaskConfig = getDayConfig()

    /** 根据配置重建某天的任务行 */
    suspend fun rebuildDayTasks(time: Long, config: DailyTaskConfig) {
        val dateKey = DateUtil.dateKey(time)
        db.taskDao().deleteByDate(dateKey)
        for (def in AppTasks.dailyTaskTemplates) {
            if (config.isIncluded(def.metricCode)) {
                db.taskDao().upsertTask(
                    DailyTaskEntity(
                        id = IdGenerator.gen("task_"),
                        date = dateKey,
                        taskId = def.id,
                        tier = "basic",
                        metric = def.metricCode,
                        target = config.getTarget(def.metricCode),
                        xpReward = def.xpReward
                    )
                )
            }
        }
    }

    /** 刷新今日任务进度 (基于 Settings 中的每日数据), 返回新完成的任务列表 */
    suspend fun refreshTodayProgress(): List<DailyTaskEntity> {
        ensureTodayTasks()
        val today = DateUtil.dateKey()
        val tasks = db.taskDao().getByDate(today)
        val newlyCompleted = mutableListOf<DailyTaskEntity>()

        for (task in tasks) {
            val count = getMetricCount(task.metric)
            val wasCompleted = task.completed
            val isCompleted = count >= task.target
            db.taskDao().updateProgress(task.id, count, isCompleted)

            if (!wasCompleted && isCompleted) {
                newlyCompleted.add(task.copy(progress = count, completed = true))
            }
        }
        return newlyCompleted
    }

    /** 检查今日全部基础任务是否已完成 */
    suspend fun checkAllTasksCompleted(): Boolean {
        ensureTodayProgress()
        val today = DateUtil.dateKey()
        val tasks = db.taskDao().getByDate(today)
        if (tasks.isEmpty()) return false

        for (task in tasks) {
            val count = getMetricCount(task.metric)
            if (count < task.target) return false
        }
        return true
    }

    /** 确保今日进度已刷新 (只更新进度) */
    suspend fun ensureTodayProgress() {
        val today = DateUtil.dateKey()
        val tasks = db.taskDao().getByDate(today)
        for (task in tasks) {
            val count = getMetricCount(task.metric)
            val isCompleted = count >= task.target
            db.taskDao().updateProgress(task.id, count, isCompleted)
        }
    }

    /** 标记今日全部完成 */
    suspend fun markTodayAllCompleted() {
        val dateKey = DateUtil.dateKey()
        db.settingDao().setInt(SettingsKeys.taskConfig(dateKey, "all_completed"), 1)
    }

    /** 检查今日是否已标记为全部完成 */
    suspend fun isTodayAllCompleted(): Boolean {
        val dateKey = DateUtil.dateKey()
        return db.settingDao().getInt(SettingsKeys.taskConfig(dateKey, "all_completed")) != 0
    }

    /** 获取今日执行度 (只计算参与的基础任务) */
    suspend fun getTodayExecutionRate(): Double {
        val tasks = db.taskDao().getByDate(DateUtil.dateKey())
        if (tasks.isEmpty()) return 0.0

        var totalProgress = 0.0
        for (task in tasks) {
            val rate = if (task.target <= 0) 0.0 else (task.progress.toDouble() / task.target).coerceIn(0.0, 1.0)
            totalProgress += rate
        }
        return totalProgress / tasks.size
    }

    /** 从 Settings 读取当日指标值 */
    suspend fun getMetricCount(metricCode: String): Int {
        val dateKey = DateUtil.dateKey()
        return when (metricCode) {
            "MEET" -> db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey))
            "QUERY" -> db.settingDao().getInt(SettingsKeys.queries(dateKey))
            "DEAL" -> db.settingDao().getInt(SettingsKeys.deals(dateKey))
            else -> 0
        }
    }

    /** 沿用昨日配置 (如果昨天有配置) */
    suspend fun inheritYesterdayConfig(): Boolean {
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())
        val hasYesterdayConfig = db.settingDao().get(SettingsKeys.taskConfig(yesterdayKey, "meet_target"))
        if (hasYesterdayConfig == null) return false

        val meetTarget = db.settingDao().getInt(SettingsKeys.taskConfig(yesterdayKey, "meet_target"))
        val queryTarget = db.settingDao().getInt(SettingsKeys.taskConfig(yesterdayKey, "query_target"))
        val dealTarget = db.settingDao().getInt(SettingsKeys.taskConfig(yesterdayKey, "deal_target"))
        val includeMeet = db.settingDao().getInt(SettingsKeys.taskConfig(yesterdayKey, "include_meet")) != 0
        val includeQuery = db.settingDao().getInt(SettingsKeys.taskConfig(yesterdayKey, "include_query")) != 0
        val includeDeal = db.settingDao().getInt(SettingsKeys.taskConfig(yesterdayKey, "include_deal")) != 0

        val config = DailyTaskConfig(meetTarget, queryTarget, dealTarget, includeMeet, includeQuery, includeDeal)
        setDayConfig(System.currentTimeMillis(), config)
        return true
    }

    /** 清除今日数据: 今日计数 + 任务 XP 标记 + 配置锁定/完成标记 + 今日任务行 (累计不受影响) */
    suspend fun clearTodayData() {
        val dateKey = DateUtil.dateKey()

        // 清除今日核心计数
        db.settingDao().remove(SettingsKeys.peopleSeen(dateKey))
        db.settingDao().remove(SettingsKeys.queries(dateKey))
        db.settingDao().remove(SettingsKeys.deals(dateKey))

        // 清除今日任务 XP 发放标记 (允许重新发放)
        val all = db.settingDao().getAll()
        for (setting in all) {
            if (setting.key.startsWith("task_xp_") && setting.key.endsWith("_$dateKey")) {
                db.settingDao().remove(setting.key)
            }
        }

        // 清除今日配置锁定和完成标记 (允许重新设置)
        db.settingDao().remove(SettingsKeys.taskConfig(dateKey, "locked"))
        db.settingDao().remove(SettingsKeys.taskConfig(dateKey, "all_completed"))
        db.settingDao().remove(SettingsKeys.dailyCompletion(dateKey))
        db.settingDao().remove(SettingsKeys.dealExtraXpAwarded(dateKey))

        // 删除今日任务行 (执行度归零, 下次记录时自动重建)
        db.taskDao().deleteByDate(dateKey)
    }
}
