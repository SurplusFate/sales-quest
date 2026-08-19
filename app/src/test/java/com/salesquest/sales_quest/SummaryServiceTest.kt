package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.services.DailySummary
import com.salesquest.sales_quest.services.DailySummaryService
import com.salesquest.sales_quest.services.WeeklySummaryService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 每日总结 / 周总结服务测试
 *
 * 总结绑定具体日期, 只反映数据, 不产生 XP / 任务 / 成就
 */
@RunWith(RobolectricTestRunner::class)
class SummaryServiceTest {

    private lateinit var db: AppDatabase
    private lateinit var dailyService: DailySummaryService
    private lateinit var weeklyService: WeeklySummaryService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dailyService = DailySummaryService(db)
        weeklyService = WeeklySummaryService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ================================================================
    // 每日总结
    // ================================================================

    @Test
    fun 保存并读取总结() = runTest {
        dailyService.saveSummary(
            DailySummary(
                dateKey = "2026-08-19",
                good = "成交 1 单",
                problems = "查询偏少",
                customerFeedback = "客户对资费敏感",
                discovery = "发现校园渠道",
                improvement = "明天多跑 2 家"
            )
        )
        val s = dailyService.getSummary("2026-08-19")
        assertNotNull(s)
        assertEquals("成交 1 单", s?.good)
        assertEquals("查询偏少", s?.problems)
        assertEquals("发现校园渠道", s?.discovery)
    }

    @Test
    fun 删除总结() = runTest {
        dailyService.saveSummary(DailySummary(dateKey = "2026-08-19", good = "x"))
        dailyService.deleteSummary("2026-08-19")
        assertNull(dailyService.getSummary("2026-08-19"))
    }

    @Test
    fun 覆盖保存同一日期() = runTest {
        dailyService.saveSummary(DailySummary(dateKey = "2026-08-19", good = "第一版"))
        dailyService.saveSummary(DailySummary(dateKey = "2026-08-19", good = "第二版"))
        assertEquals("第二版", dailyService.getSummary("2026-08-19")?.good)
        assertEquals(1, dailyService.getAllSummaries().size)
    }

    @Test
    fun 空总结isEmpty为true() {
        val empty = DailySummary(dateKey = "2026-08-19")
        assertTrue(empty.isEmpty)
    }

    @Test
    fun 单日数据摘要_计算查询率成交率() = runTest {
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen("2026-08-19"), "10"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries("2026-08-19"), "5"))
        db.settingDao().set(SettingEntity(SettingsKeys.deals("2026-08-19"), "2"))

        val snap = dailyService.getDaySummary("2026-08-19")
        assertEquals(10, snap.peopleSeen)
        assertEquals(5, snap.queries)
        assertEquals(2, snap.deals)
        assertEquals(50.0, snap.queryRate, 0.001)
        assertEquals(20.0, snap.dealRate, 0.001)
    }

    @Test
    fun 无见人数_比率为0() = runTest {
        val snap = dailyService.getDaySummary("2026-08-19")
        assertEquals(0.0, snap.queryRate, 0.001)
        assertEquals(0.0, snap.dealRate, 0.001)
    }

    @Test
    fun 范围快照_累计区间数据() = runTest {
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen("2026-08-17"), "3"))
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen("2026-08-18"), "4"))
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen("2026-08-19"), "5"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries("2026-08-19"), "2"))

        val snap = dailyService.getRangeSnapshot("2026-08-17", "2026-08-19")
        assertEquals(12, snap.peopleSeen)
        assertEquals(2, snap.queries)
        assertEquals(0, snap.deals)
        assertEquals(3, snap.activeDays)
    }

    // ================================================================
    // 周总结
    // ================================================================

    @Test
    fun 周对比_返回本周与上周数据() = runTest {
        // 写入本周数据 (周一起算)
        val monday = com.salesquest.sales_quest.data.DateUtil.mondayStart()
        val dateKey = com.salesquest.sales_quest.data.DateUtil.dateKey(monday)
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen(dateKey), "10"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries(dateKey), "5"))

        val comp = weeklyService.compareThisWeek()
        assertTrue(comp.current.peopleSeen >= 10)
        assertEquals("本周", comp.current.weekLabel)
        assertEquals("上周", comp.previous.weekLabel)
        assertTrue(comp.analysis.isNotBlank())
    }

    @Test
    fun 周分析_无数据给出鼓励文案() = runTest {
        val comp = weeklyService.compareThisWeek()
        assertTrue(comp.analysis.contains("继续加油"))
    }

    @Test
    fun 周分析_见人下降查询率提升_提示沟通质量() = runTest {
        // 上周: 见人多, 查询率低
        val monday = com.salesquest.sales_quest.data.DateUtil.mondayStart()
        val weekMs = 7 * 24L * 60 * 60 * 1000
        val lastMonday = monday - weekMs
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen(com.salesquest.sales_quest.data.DateUtil.dateKey(lastMonday)), "100"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries(com.salesquest.sales_quest.data.DateUtil.dateKey(lastMonday)), "10"))
        // 本周: 见人少, 查询率高
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen(com.salesquest.sales_quest.data.DateUtil.dateKey(monday)), "10"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries(com.salesquest.sales_quest.data.DateUtil.dateKey(monday)), "5"))

        val comp = weeklyService.compareThisWeek()
        assertTrue(comp.analysis.contains("有效沟通质量有所提高"))
    }

    @Test
    fun 周分析_见人下降_建议增加频次() = runTest {
        val monday = com.salesquest.sales_quest.data.DateUtil.mondayStart()
        val weekMs = 7 * 24L * 60 * 60 * 1000
        val lastMonday = monday - weekMs
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen(com.salesquest.sales_quest.data.DateUtil.dateKey(lastMonday)), "100"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries(com.salesquest.sales_quest.data.DateUtil.dateKey(lastMonday)), "100"))
        db.settingDao().set(SettingEntity(SettingsKeys.peopleSeen(com.salesquest.sales_quest.data.DateUtil.dateKey(monday)), "10"))
        db.settingDao().set(SettingEntity(SettingsKeys.queries(com.salesquest.sales_quest.data.DateUtil.dateKey(monday)), "1"))

        val comp = weeklyService.compareThisWeek()
        assertTrue(comp.analysis.contains("建议增加外访频次"))
    }
}
