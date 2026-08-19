package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.ConfigKeys
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.ConfigImportResult
import com.salesquest.sales_quest.services.ConfigService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 配置文件导入/导出服务测试
 *
 * 原则: 配置导入后写入内部 DB, 删除原 JSON 文件不影响使用
 */
@RunWith(RobolectricTestRunner::class)
class ConfigServiceTest {

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

    @Test
    fun 导出JSON_包含任务配置与等级数据() = runTest {
        val json = service.exportConfigJson()
        assertTrue(json.contains("\"taskConfig\""))
        assertTrue(json.contains("\"levels\""))
        assertTrue(json.contains("\"version\""))
        assertTrue(json.contains("\"meetTarget\""))
        assertTrue(json.contains("\"conditions\""))
    }

    @Test
    fun 导入有效配置_写入数据库且返回成功() = runTest {
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
                {"level": 3, "title": "需求诊断师", "xpRequired": 300,
                 "conditions": [{"type": "XP", "threshold": 300}, {"type": "TOTAL_MEET", "threshold": 40}]}
              ]
            }
        """.trimIndent()

        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.Success)
        assertEquals(80, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
        assertEquals(10, db.settingDao().getInt(SettingsKeys.DEFAULT_QUERY_TARGET))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.DEFAULT_DEAL_TARGET))

        val reqs = db.levelRequirementDao().getForLevel(3)
        assertEquals(2, reqs.size)
        assertEquals("TOTAL_MEET", reqs.first { it.conditionType == "TOTAL_MEET" }.conditionType)
        assertEquals(40, reqs.first { it.conditionType == "TOTAL_MEET" }.threshold)
        assertEquals(ConfigKeys.CONFIG_VERSION, db.settingDao().getInt(ConfigKeys.IMPORTED_CONFIG_VERSION))
        assertNotNull(db.settingDao().get(ConfigKeys.IMPORTED_CONFIG_AT))
    }

    @Test
    fun 导入版本不兼容_返回版本错误且不写库() = runTest {
        val raw = """{"version": 99, "taskConfig": {"meetTarget": 80, "queryTarget": 10, "dealTarget": 2,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()

        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.VersionError)
        assertEquals(99, (result as ConfigImportResult.VersionError).found)
        assertEquals(0, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
    }

    @Test
    fun 导入格式错误_返回格式错误() = runTest {
        val result = service.importConfigJson("not a json {{{")
        assertTrue(result is ConfigImportResult.FormatError)
    }

    @Test
    fun 导入负目标任务_返回校验错误() = runTest {
        val raw = """{"version": 1, "taskConfig": {"meetTarget": -1, "queryTarget": 10, "dealTarget": 2,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()
        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.ValidationError)
    }

    @Test
    fun 导入未知条件类型_返回校验错误() = runTest {
        val raw = """{"version": 1, "levels": [{"level": 3, "title": "x", "xpRequired": 300,
            "conditions": [{"type": "HACK", "threshold": 5}]}]}""".trimIndent()
        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.ValidationError)
    }

    @Test
    fun 导入负阈值_返回校验错误() = runTest {
        val raw = """{"version": 1, "levels": [{"level": 3, "title": "x", "xpRequired": 300,
            "conditions": [{"type": "TOTAL_MEET", "threshold": -5}]}]}""".trimIndent()
        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.ValidationError)
    }

    @Test
    fun 导入后清空原JSON_仅依赖内部数据仍可用() = runTest {
        val raw = """{"version": 1, "taskConfig": {"meetTarget": 100, "queryTarget": 20, "dealTarget": 3,
            "includeMeet": true, "includeQuery": true, "includeDeal": true}}""".trimIndent()
        val result = service.importConfigJson(raw)
        assertTrue(result is ConfigImportResult.Success)

        // 模拟: 原 JSON 文件已删除, 但配置已持久化在 DB
        assertEquals(100, db.settingDao().getInt(SettingsKeys.DEFAULT_MEET_TARGET))
        assertEquals(20, db.settingDao().getInt(SettingsKeys.DEFAULT_QUERY_TARGET))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.DEFAULT_DEAL_TARGET))
    }

    @Test
    fun 导出再导入_数据往返一致() = runTest {
        val json = service.exportConfigJson()
        val result = service.importConfigJson(json)
        assertTrue(result is ConfigImportResult.Success)
    }
}
