package com.salesquest.sales_quest.services

import androidx.room.withTransaction
import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.core.ConfigKeys
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.IdGenerator
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 配置文件数据结构 (与 sales_quest_config.json 对应)
 */
@Serializable
data class ConfigFile(
    val version: Int = ConfigKeys.CONFIG_VERSION,
    val taskConfig: TaskConfigData? = null,
    val levels: List<LevelConfigData> = emptyList()
)

@Serializable
data class TaskConfigData(
    val meetTarget: Int,
    val queryTarget: Int,
    val dealTarget: Int,
    val includeMeet: Boolean,
    val includeQuery: Boolean,
    val includeDeal: Boolean
)

@Serializable
data class LevelConfigData(
    val level: Int,
    val title: String,
    val xpRequired: Int,
    val conditions: List<ConditionData> = emptyList()
)

@Serializable
data class ConditionData(
    val type: String,
    val threshold: Int
)

/** 导入结果 */
sealed class ConfigImportResult {
    data class Success(val version: Int) : ConfigImportResult()
    data class FormatError(val message: String) : ConfigImportResult()
    data class VersionError(val found: Int, val supported: Int) : ConfigImportResult()
    data class ValidationError(val message: String) : ConfigImportResult()
}

/**
 * 配置文件导入/导出服务
 *
 * 原则: 配置文件只控制「App 怎么运行」, 数据存于数据库。
 * 导入后即使删除原 JSON 文件, App 仍可正常运行 (已写入内部 DB)。
 */
class ConfigService(
    private val db: AppDatabase,
    private val onDataChanged: () -> Unit = {}
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ==================== 导出 ====================

    /** 生成配置 JSON 字符串 */
    suspend fun exportConfigJson(): String {
        val config = buildConfigFile()
        return json.encodeToString(ConfigFile.serializer(), config)
    }

    suspend fun buildConfigFile(): ConfigFile {
        val defaultCfg = db.settingDao().let { dao ->
            DailyTaskConfig(
                meetTarget = dao.getInt(SettingsKeys.DEFAULT_MEET_TARGET),
                queryTarget = dao.getInt(SettingsKeys.DEFAULT_QUERY_TARGET),
                dealTarget = dao.getInt(SettingsKeys.DEFAULT_DEAL_TARGET),
                includeMeet = dao.get(SettingsKeys.DEFAULT_INCLUDE_MEET) == "1",
                includeQuery = dao.get(SettingsKeys.DEFAULT_INCLUDE_QUERY) == "1",
                includeDeal = dao.get(SettingsKeys.DEFAULT_INCLUDE_DEAL) == "1"
            )
        }

        val levelData = AppLevels.levels.map { lv ->
            val reqs = db.levelRequirementDao().getForLevel(lv.level)
            LevelConfigData(
                level = lv.level,
                title = lv.title,
                xpRequired = lv.xpRequired,
                conditions = if (reqs.isEmpty()) {
                    AppLevels.defaultRequirements.filter { it.level == lv.level }
                        .map { ConditionData(it.conditionType.name, it.threshold) }
                } else {
                    reqs.map { ConditionData(it.conditionType, it.threshold) }
                }
            )
        }

        return ConfigFile(
            version = ConfigKeys.CONFIG_VERSION,
            taskConfig = TaskConfigData(
                meetTarget = defaultCfg.meetTarget,
                queryTarget = defaultCfg.queryTarget,
                dealTarget = defaultCfg.dealTarget,
                includeMeet = defaultCfg.includeMeet,
                includeQuery = defaultCfg.includeQuery,
                includeDeal = defaultCfg.includeDeal
            ),
            levels = levelData
        )
    }

    // ==================== 导入 ====================

    /**
     * 解析 + 校验 + 应用配置。失败返回错误, 成功返回 Success
     *
     * 原则: 先验证后修改, 应用配置使用 Transaction 保证原子性 (要么全部成功, 要么不改变原配置)
     */
    suspend fun importConfigJson(raw: String): ConfigImportResult {
        // 1. 解析 JSON
        val config = try {
            json.decodeFromString(ConfigFile.serializer(), raw)
        } catch (e: Exception) {
            return ConfigImportResult.FormatError("配置文件格式错误: ${e.message}")
        }

        // 2. 版本校验
        if (config.version != ConfigKeys.CONFIG_VERSION) {
            return ConfigImportResult.VersionError(config.version, ConfigKeys.CONFIG_VERSION)
        }

        // 3. 完整性 + 合法性校验 (在任何 DB 写入之前)
        validate(config)?.let { return it }

        // 4. 原子性应用: 整个 Transaction 成功才 COMMIT, 任一步失败自动 ROLLBACK
        db.withTransaction {
            applyConfig(config)
            db.settingDao().setInt(ConfigKeys.IMPORTED_CONFIG_VERSION, config.version)
            db.settingDao().set(com.salesquest.sales_quest.data.entity.SettingEntity(
                key = ConfigKeys.IMPORTED_CONFIG_AT,
                value = System.currentTimeMillis().toString()
            ))
        }

        onDataChanged()
        return ConfigImportResult.Success(config.version)
    }

    private fun validate(config: ConfigFile): ConfigImportResult.ValidationError? {
        config.taskConfig?.let { tc ->
            val targets = listOf(tc.meetTarget, tc.queryTarget, tc.dealTarget)
            if (targets.any { it < 0 }) {
                return ConfigImportResult.ValidationError("任务目标不能为负数")
            }
            if (targets.any { it > 100000 }) {
                return ConfigImportResult.ValidationError("任务目标超出范围 (0-100000)")
            }
            // 销售漏斗关系: 成交目标 ≤ 查询目标 ≤ 见人目标
            if (tc.queryTarget > tc.meetTarget) {
                return ConfigImportResult.ValidationError("查询目标 (${tc.queryTarget}) 不能大于见人目标 (${tc.meetTarget})")
            }
            if (tc.dealTarget > tc.queryTarget) {
                return ConfigImportResult.ValidationError("成交目标 (${tc.dealTarget}) 不能大于查询目标 (${tc.queryTarget})")
            }
        }

        for (lv in config.levels) {
            if (lv.level < 1) return ConfigImportResult.ValidationError("等级必须大于 0")
            if (lv.xpRequired < 0) return ConfigImportResult.ValidationError("等级 ${lv.level} 的 XP 门槛不能为负数")
            for (cond in lv.conditions) {
                val validTypes = setOf("XP", "TOTAL_MEET", "TOTAL_QUERY", "TOTAL_DEAL", "STREAK_DAYS")
                if (cond.type !in validTypes) {
                    return ConfigImportResult.ValidationError("等级 ${lv.level} 含未知条件类型: ${cond.type}")
                }
                if (cond.threshold < 0) {
                    return ConfigImportResult.ValidationError("等级 ${lv.level} 的条件阈值不能为负数")
                }
            }
        }
        return null
    }

    /** 写入内部数据库: 每日任务默认配置 + 晋级条件 */
    private suspend fun applyConfig(config: ConfigFile) {
        config.taskConfig?.let { tc ->
            db.settingDao().setInt(SettingsKeys.DEFAULT_MEET_TARGET, tc.meetTarget)
            db.settingDao().setInt(SettingsKeys.DEFAULT_QUERY_TARGET, tc.queryTarget)
            db.settingDao().setInt(SettingsKeys.DEFAULT_DEAL_TARGET, tc.dealTarget)
            db.settingDao().setInt(SettingsKeys.DEFAULT_INCLUDE_MEET, if (tc.includeMeet) 1 else 0)
            db.settingDao().setInt(SettingsKeys.DEFAULT_INCLUDE_QUERY, if (tc.includeQuery) 1 else 0)
            db.settingDao().setInt(SettingsKeys.DEFAULT_INCLUDE_DEAL, if (tc.includeDeal) 1 else 0)
        }

        // 晋级条件: 先清空再写入配置的条件
        db.levelRequirementDao().clearAll()
        val hasAnyCondition = config.levels.any { it.conditions.isNotEmpty() }
        if (hasAnyCondition) {
            for (lv in config.levels) {
                for (cond in lv.conditions) {
                    db.levelRequirementDao().insert(
                        LevelRequirementEntity(
                            id = IdGenerator.gen("lvl_"),
                            level = lv.level,
                            conditionType = cond.type,
                            threshold = cond.threshold
                        )
                    )
                }
            }
        }
    }
}
