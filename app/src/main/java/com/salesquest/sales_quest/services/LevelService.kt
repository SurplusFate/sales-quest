package com.salesquest.sales_quest.services

import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.core.LevelConditionType
import com.salesquest.sales_quest.core.LevelDef
import com.salesquest.sales_quest.core.LevelRequirement
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity
import com.salesquest.sales_quest.data.entity.UserStatEntity
import com.salesquest.sales_quest.data.entity.XpRecordEntity

/** 单条晋级条件进度 */
data class RequirementProgress(
    val type: LevelConditionType,
    val current: Int,
    val threshold: Int,
    val met: Boolean
) {
    val label: String get() = type.label
}

/** 等级进度 (当前等级 + 距下一级各条件进度) */
data class LevelProgress(
    val currentLevel: LevelDef,
    val nextLevel: LevelDef?,
    val totalXp: Int,
    val requirements: List<RequirementProgress>
) {
    val isMaxLevel: Boolean get() = nextLevel == null
}

/**
 * 等级晋级条件服务 (v2)
 *
 * 核心变更:
 * 1. 升级不再只看 XP, 必须同时满足当前等级的全部晋级条件 (XP + 累计指标)
 * 2. 条件配置化, 存于 level_requirements 表; 未配置时使用 AppLevels.defaultRequirements
 * 3. 升级判定为纯函数, 便于单元测试
 */
class LevelService(private val db: AppDatabase) {

    /** 读取已配置的晋级条件, 无配置时返回默认 */
    suspend fun getRequirements(): List<LevelRequirement> {
        val configured = db.levelRequirementDao().getAll()
        if (configured.isEmpty()) return AppLevels.defaultRequirements
        return configured.map { it.toLevelRequirement() }
    }

    /** 当前用户进度 (从 DB 读取统计数据) */
    suspend fun getProgress(): LevelProgress {
        val stats = db.statsDao().getStats()
        val xp = stats?.totalXp ?: 0
        val streak = stats?.streakDays ?: 0
        val totalMeet = db.settingDao().getInt(SettingsKeys.TOTAL_MEETS)
        val totalQuery = db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES)
        val totalDeal = db.settingDao().getInt(SettingsKeys.TOTAL_DEALS)
        return buildProgress(getRequirements(), xp, totalMeet, totalQuery, totalDeal, streak)
    }

    /** 获取晋级下一级所需的全部条件 (含 XP), 供 UI 展示 */
    suspend fun getNextLevelRequirements(): List<LevelRequirement> {
        val progress = getProgress()
        val nextLevel = progress.nextLevel ?: return emptyList()
        return getRequirements().filter { it.level == nextLevel.level }
    }

    /** 纯函数: 计算当前等级 (满足条件的最高等级) */
    companion object {
        fun evaluateCurrentLevel(
            requirements: List<LevelRequirement>,
            totalXp: Int,
            totalMeet: Int,
            totalQuery: Int,
            totalDeal: Int,
            streakDays: Int
        ): Int {
            val reqByLevel = requirements.groupBy { it.level }
            var currentLevel = 1
            for (lv in AppLevels.levels) {
                val reqs = reqByLevel[lv.level].orEmpty()
                if (reqs.isEmpty()) {
                    // 无条件的等级: 仅 XP 达标即可
                    if (totalXp >= lv.xpRequired) currentLevel = lv.level
                    continue
                }
                val allMet = reqs.all { req ->
                    when (req.conditionType) {
                        LevelConditionType.XP -> totalXp >= req.threshold
                        LevelConditionType.TOTAL_MEET -> totalMeet >= req.threshold
                        LevelConditionType.TOTAL_QUERY -> totalQuery >= req.threshold
                        LevelConditionType.TOTAL_DEAL -> totalDeal >= req.threshold
                        LevelConditionType.STREAK_DAYS -> streakDays >= req.threshold
                    }
                }
                if (allMet) currentLevel = lv.level
            }
            return currentLevel
        }

        /** 纯函数: 构建等级进度 */
        fun buildProgress(
            requirements: List<LevelRequirement>,
            totalXp: Int,
            totalMeet: Int,
            totalQuery: Int,
            totalDeal: Int,
            streakDays: Int
        ): LevelProgress {
            val currentLevelNum = evaluateCurrentLevel(requirements, totalXp, totalMeet, totalQuery, totalDeal, streakDays)
            val currentLevel = AppLevels.levels.first { it.level == currentLevelNum }
            val nextLevel = AppLevels.levels.firstOrNull { it.level == currentLevelNum + 1 }

            val nextReqs = requirements.filter { it.level == currentLevelNum + 1 }
            val reqProgress = nextReqs.map { req ->
                val current = when (req.conditionType) {
                    LevelConditionType.XP -> totalXp
                    LevelConditionType.TOTAL_MEET -> totalMeet
                    LevelConditionType.TOTAL_QUERY -> totalQuery
                    LevelConditionType.TOTAL_DEAL -> totalDeal
                    LevelConditionType.STREAK_DAYS -> streakDays
                }
                RequirementProgress(
                    type = req.conditionType,
                    current = current,
                    threshold = req.threshold,
                    met = current >= req.threshold
                )
            }.sortedWith(compareBy<RequirementProgress> { it.type.ordinal }.thenBy { it.threshold })

            return LevelProgress(currentLevel, nextLevel, totalXp, reqProgress)
        }
    }
}

private fun LevelRequirementEntity.toLevelRequirement(): LevelRequirement {
    val type = when (conditionType) {
        "TOTAL_MEET" -> LevelConditionType.TOTAL_MEET
        "TOTAL_QUERY" -> LevelConditionType.TOTAL_QUERY
        "TOTAL_DEAL" -> LevelConditionType.TOTAL_DEAL
        "STREAK_DAYS" -> LevelConditionType.STREAK_DAYS
        else -> LevelConditionType.XP
    }
    return LevelRequirement(level, type, threshold)
}
