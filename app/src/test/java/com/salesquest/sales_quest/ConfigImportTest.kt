package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.LevelConditionType
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.ConfigImportResult
import com.salesquest.sales_quest.services.ConfigService
import com.salesquest.sales_quest.services.LevelService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * P0 配置导入原子性与校验测试
 *
 * 核心原则:
 * 1. 配置导入「先校验后写入」, 校验失败绝不修改原有配置 (原子性)
 * 2. 部分等级配置时, 未配置等级回退到 AppLevels.defaultRequirements, 不能为空
 *
 * 配置 JSON 格式:
 * {
 *   "version": 1,
 *   "taskConfig": { "meetTarget": 80, "queryTarget": 10, "dealTarget": 2,
 *                   "includeMeet": true, "includeQuery": true, "includeDeal": true },
 *   "levels": [ {"level": 3, "title": "需求诊断师", "xpRequired": 300,
 *                "conditions": [{"type": "XP", "threshold": 300}]} ]
 * }
 */
@RunWith(RobolectricTestRunner::class)
class ConfigImportTest {

    private lateinit var db: AppDatabase
    private lateinit var service: ConfigService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = ConfigService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ================================================================
    // TEST 1: 有效配置完整导入
    // ================================================================
    @Test
    fun 有效配置完整导入_写入数据库并返回成功() = runTest {
        val raw = """
            {
              "version": 1,
              "taskConfig": {
                "meetTarget": 80,
                "queryTarget": 10,
                "dealTarget": 2,
                "includeMeet": true,
                "includeQuery": true,
                "includeDeal": true
              },
              "levels": [
                {"level": 3, "title": "需求诊断师", "xpRequired": 300, "conditions": [{"type": "XP", "threshold": 300}]}
              ]
            }
        """.trimIndent()

        val result = service.importConfigJson(raw)

        assertTrue(result is ConfigImportResult.Success)
        assertEquals(80, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
        assertEquals(10, db.settingDao().getInt(SettingsKeys.DEFAULT_QUERY_TARGET))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.DEFAULT_DEAL_TARGET))
    }

    // ================================================================
    // TEST 2: 漏斗违例 (查询 > 见人) 不修改原配置
    // ================================================================
    @Test
    fun 漏斗违例_查询大于见人_不修改原配置() = runTest {
        // 1. 先导入一份有效配置
        val valid = """{"version": 1, "taskConfig": {"meetTarget": 100, "queryTarget": 5, "dealTarget": 1,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()
        val first = service.importConfigJson(valid)
        assertTrue(first is ConfigImportResult.Success)

        // 2. 再尝试导入违例配置 (queryTarget=100 > meetTarget=5, 违反销售漏斗)
        val invalid = """{"version": 1, "taskConfig": {"meetTarget": 5, "queryTarget": 100, "dealTarget": 1,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()
        val result = service.importConfigJson(invalid)

        assertTrue(result is ConfigImportResult.ValidationError)

        // 3. 原配置应保持不变 (原子性: 校验失败不写库)
        assertEquals(100, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
        assertEquals(5, db.settingDao().getInt(SettingsKeys.DEFAULT_QUERY_TARGET))
        assertEquals(1, db.settingDao().getInt(SettingsKeys.DEFAULT_DEAL_TARGET))
    }

    // ================================================================
    // TEST 3: 漏斗违例 (成交 > 查询) 不修改原配置
    // ================================================================
    @Test
    fun 漏斗违例_成交大于查询_不修改原配置() = runTest {
        // 1. 先导入一份有效配置
        val valid = """{"version": 1, "taskConfig": {"meetTarget": 100, "queryTarget": 5, "dealTarget": 1,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()
        val first = service.importConfigJson(valid)
        assertTrue(first is ConfigImportResult.Success)

        // 2. 再尝试导入违例配置 (dealTarget=10 > queryTarget=5)
        val invalid = """{"version": 1, "taskConfig": {"meetTarget": 100, "queryTarget": 5, "dealTarget": 10,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()
        val result = service.importConfigJson(invalid)

        assertTrue(result is ConfigImportResult.ValidationError)

        // 3. 原配置应保持不变
        assertEquals(100, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
        assertEquals(5, db.settingDao().getInt(SettingsKeys.DEFAULT_QUERY_TARGET))
        assertEquals(1, db.settingDao().getInt(SettingsKeys.DEFAULT_DEAL_TARGET))
    }

    // ================================================================
    // TEST 4: 负数目标被拒绝
    // ================================================================
    @Test
    fun 负数目标_返回校验错误() = runTest {
        val raw = """{"version": 1, "taskConfig": {"meetTarget": -1, "queryTarget": 10, "dealTarget": 2,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()

        val result = service.importConfigJson(raw)

        assertTrue(result is ConfigImportResult.ValidationError)
    }

    // ================================================================
    // TEST 5: 部分等级配置, 未配置等级回退默认条件 (不能为空)
    // ================================================================
    @Test
    fun 部分等级配置_未配置等级回退默认条件() = runTest {
        // 仅配置 Level 2 的 XP 条件 (门槛 50), 其它等级不配置
        val raw = """
            {
              "version": 1,
              "levels": [
                {"level": 2, "title": "沟通学徒", "xpRequired": 100, "conditions": [{"type": "XP", "threshold": 50}]}
              ]
            }
        """.trimIndent()

        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.Success)

        val levelService = LevelService(db)
        val reqs = levelService.getRequirements()

        // Level 2: 使用自定义条件 (XP 门槛 50)
        val lv2Xp = reqs.first { it.level == 2 && it.conditionType == LevelConditionType.XP }
        assertEquals(50, lv2Xp.threshold)

        // Level 3: 未配置, 回退默认条件 (XP 300 + TOTAL_MEET 50 + TOTAL_QUERY 10), 不能为空
        val lv3Reqs = reqs.filter { it.level == 3 }
        assertTrue("Level 3 未配置时应回退默认条件, 不能为空", lv3Reqs.isNotEmpty())
        assertEquals(300, lv3Reqs.first { it.conditionType == LevelConditionType.XP }.threshold)
        assertEquals(50, lv3Reqs.first { it.conditionType == LevelConditionType.TOTAL_MEET }.threshold)
        assertEquals(10, lv3Reqs.first { it.conditionType == LevelConditionType.TOTAL_QUERY }.threshold)

        // Level 4: 未配置, 回退默认条件 (XP 600 + TOTAL_MEET 150 + TOTAL_QUERY 30), 不能为空
        val lv4Reqs = reqs.filter { it.level == 4 }
        assertTrue("Level 4 未配置时应回退默认条件, 不能为空", lv4Reqs.isNotEmpty())
        assertEquals(600, lv4Reqs.first { it.conditionType == LevelConditionType.XP }.threshold)
        assertEquals(150, lv4Reqs.first { it.conditionType == LevelConditionType.TOTAL_MEET }.threshold)
        assertEquals(30, lv4Reqs.first { it.conditionType == LevelConditionType.TOTAL_QUERY }.threshold)

        // 关键断言: 整体回退到默认条件, 而非仅有 Level 2 一条
        assertTrue("未配置等级应回退 defaultRequirements, 总数应大于仅 Level 2 的 1 条", reqs.size > 1)
    }

    // ================================================================
    // TEST 6: 仅任务配置 (无 levels) 仍可导入
    // ================================================================
    @Test
    fun 仅任务配置无levels_仍可导入() = runTest {
        val raw = """{"version": 1, "taskConfig": {"meetTarget": 80, "queryTarget": 10, "dealTarget": 2,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()

        val result = service.importConfigJson(raw)

        assertTrue(result is ConfigImportResult.Success)
        assertEquals(80, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
        assertEquals(10, db.settingDao().getInt(SettingsKeys.DEFAULT_QUERY_TARGET))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.DEFAULT_DEAL_TARGET))
    }
}
