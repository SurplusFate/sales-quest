package com.salesquest.sales_quest.core

/**
 * V1.0.0 应用常量: 等级、任务模板、成就定义
 */

/** 等级定义 */
data class LevelDef(val level: Int, val title: String, val xpRequired: Int)

/** 晋级条件类型 */
enum class LevelConditionType(val label: String) {
    XP("XP"),
    TOTAL_MEET("累计见人"),
    TOTAL_QUERY("累计查询"),
    TOTAL_DEAL("累计成交"),
    STREAK_DAYS("连续作战天数")
}

/** 晋级条件 (配置化) */
data class LevelRequirement(
    val level: Int,
    val conditionType: LevelConditionType,
    val threshold: Int
)

object AppLevels {
    val levels: List<LevelDef> = listOf(
        LevelDef(1, "销售新人", 0),
        LevelDef(2, "沟通学徒", 100),
        LevelDef(3, "需求诊断师", 300),
        LevelDef(4, "查询猎手", 600),
        LevelDef(5, "成交高手", 1200),
        LevelDef(6, "销售达人", 2000),
        LevelDef(7, "销售大师", 3500),
        LevelDef(8, "销售王者", 6000)
    )

    /** 默认晋级条件 (Lv2 起, 配置化; 导入配置可覆盖) */
    val defaultRequirements: List<LevelRequirement> = listOf(
        LevelRequirement(2, LevelConditionType.XP, 100),
        LevelRequirement(3, LevelConditionType.XP, 300),
        LevelRequirement(3, LevelConditionType.TOTAL_MEET, 50),
        LevelRequirement(3, LevelConditionType.TOTAL_QUERY, 10),
        LevelRequirement(4, LevelConditionType.XP, 600),
        LevelRequirement(4, LevelConditionType.TOTAL_MEET, 150),
        LevelRequirement(4, LevelConditionType.TOTAL_QUERY, 30),
        LevelRequirement(5, LevelConditionType.XP, 1200),
        LevelRequirement(5, LevelConditionType.TOTAL_QUERY, 100),
        LevelRequirement(5, LevelConditionType.TOTAL_DEAL, 5),
        LevelRequirement(6, LevelConditionType.XP, 2000),
        LevelRequirement(6, LevelConditionType.TOTAL_DEAL, 15),
        LevelRequirement(7, LevelConditionType.XP, 3500),
        LevelRequirement(7, LevelConditionType.TOTAL_MEET, 800),
        LevelRequirement(8, LevelConditionType.XP, 6000),
        LevelRequirement(8, LevelConditionType.TOTAL_DEAL, 40)
    )

    fun getLevel(totalXp: Int): LevelDef {
        var result = levels[0]
        for (lv in levels) {
            if (totalXp >= lv.xpRequired) result = lv
        }
        return result
    }

    fun getNextLevel(totalXp: Int): LevelDef? {
        val current = getLevel(totalXp)
        val idx = levels.indexOf(current)
        return if (idx < levels.size - 1) levels[idx + 1] else null
    }

    fun getProgress(totalXp: Int): Double {
        val current = getLevel(totalXp)
        val next = getNextLevel(totalXp) ?: return 1.0
        val range = (next.xpRequired - current.xpRequired).toDouble()
        val progress = (totalXp - current.xpRequired).toDouble()
        return if (range <= 0) 1.0 else (progress / range).coerceIn(0.0, 1.0)
    }
}

/** 每日任务定义 (用于创建任务行时的元数据) */
data class DailyTaskDef(
    val id: String,
    val metricCode: String, // CoreMetric.code
    val label: String,
    val xpReward: Int
)

/** 任务模板: 只有元数据, target 由用户配置决定 */
object AppTasks {
    val dailyTaskTemplates: List<DailyTaskDef> = listOf(
        DailyTaskDef(id = "task_meet", metricCode = "MEET", label = "见人", xpReward = 100),
        DailyTaskDef(id = "task_query", metricCode = "QUERY", label = "查询", xpReward = 80),
        DailyTaskDef(id = "task_deal", metricCode = "DEAL", label = "成交", xpReward = 200)
    )

    fun findByMetric(metricCode: String): DailyTaskDef? =
        dailyTaskTemplates.firstOrNull { it.metricCode == metricCode }
}

/** 推荐默认任务配置 */
object DefaultTaskConfig {
    const val recommendedMeetTarget = 100
    const val recommendedQueryTarget = 5
    const val recommendedDealTarget = 1
    const val recommendedIncludeMeet = true
    const val recommendedIncludeQuery = true
    const val recommendedIncludeDeal = false
}

/** XP 奖励常量 */
object XpRewards {
    const val dailyCompletionBonus = 50
    const val dealExtraXp = 50
}

/** 成就类型 */
enum class AchievementType(val label: String) {
    TOTAL_MEET("总见人数"),
    TOTAL_QUERY("总查询数"),
    TOTAL_DEAL("总成交数"),
    STREAK_DAYS("连续作战天数"),
    DAILY_QUERY("单日查询数"),
    DAILY_DEAL("单日成交数"),
    FIRST_MEET("首次见人"),
    FIRST_QUERY("首次查询"),
    FIRST_DEAL("首次成交")
}

/** 成就定义 */
data class AchievementDef(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val target: Int
)

object AppAchievements {
    val definitions: List<AchievementDef> = listOf(
        AchievementDef("first_meet", "👋", "第一声", "完成第一次见人", AchievementType.FIRST_MEET, 1),
        AchievementDef("first_query", "🔎", "第一次查询", "完成第一次客户查询", AchievementType.FIRST_QUERY, 1),
        AchievementDef("first_deal", "🎉", "首单成交", "完成第一次成交", AchievementType.FIRST_DEAL, 1),
        AchievementDef("streak_7", "🔥", "连续作战", "连续 7 天完成每日基础任务", AchievementType.STREAK_DAYS, 7),
        AchievementDef("daily_query_10", "🎯", "查询猎手", "一天完成 10 次查询", AchievementType.DAILY_QUERY, 10),
        AchievementDef("daily_deal_3", "🏆", "成交日", "一天完成 3 次成交", AchievementType.DAILY_DEAL, 3),
        AchievementDef("total_meet_1000", "👥", "千人斩", "累计见人 1000 次", AchievementType.TOTAL_MEET, 1000),
        AchievementDef("total_query_100", "🩺", "诊断师", "累计完成 100 次查询", AchievementType.TOTAL_QUERY, 100)
    )
}

/** Settings 存储键约定 */
object SettingsKeys {
    const val TOTAL_MEETS = "total_meets"
    const val TOTAL_QUERIES = "total_queries"
    const val TOTAL_DEALS = "total_deals"

    fun peopleSeen(dateKey: String) = "people_seen_$dateKey"
    fun queries(dateKey: String) = "queries_$dateKey"
    fun deals(dateKey: String) = "deals_$dateKey"

    const val DEFAULT_MEET_TARGET = "default_meet_target"
    const val DEFAULT_QUERY_TARGET = "default_query_target"
    const val DEFAULT_DEAL_TARGET = "default_deal_target"
    const val DEFAULT_INCLUDE_MEET = "default_include_meet"
    const val DEFAULT_INCLUDE_QUERY = "default_include_query"
    const val DEFAULT_INCLUDE_DEAL = "default_include_deal"

    fun taskConfig(dateKey: String, suffix: String) = "task_config_${dateKey}_$suffix"
    fun taskXp(taskId: String, dateKey: String) = "task_xp_${taskId}_$dateKey"
    fun dailyCompletion(dateKey: String) = "daily_completion_$dateKey"
    fun dealExtraXpAwarded(dateKey: String) = "deal_extra_xp_awarded_$dateKey"

    /** 客户编号计数器: 记录历史最大已分配编号 (删除客户不回退) */
    const val MAX_CUSTOMER_NUMBER = "max_customer_number"
}

/** 配置文件导入/导出相关键 */
object ConfigKeys {
    const val CONFIG_VERSION = 1
    const val IMPORTED_CONFIG_VERSION = "imported_config_version"
    const val IMPORTED_CONFIG_AT = "imported_config_at"
}

/** 云备份配置键 (密码走 EncryptedSharedPreferences) */
object BackupKeys {
    const val PREFS_BACKUP = "backup_prefs"
    const val WEBDAV_URL = "webdav_url"
    const val WEBDAV_USERNAME = "webdav_username"
    const val WEBDAV_PASSWORD = "webdav_password"
    const val WEBDAV_DIR = "webdav_dir"
    const val AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val LAST_BACKUP_AT = "last_backup_at"
    const val PENDING_BACKUP = "pending_backup"
    const val BACKUP_FILENAME_PREFIX = "sales_quest_backup_"
    const val DB_BACKUP_SUFFIX = ".db"
}

/** 云备份默认配置 */
object BackupDefaults {
    const val DEFAULT_WEBDAV_DIR = "/SalesQuest"
    const val AUTO_BACKUP_DAILY = true
}
