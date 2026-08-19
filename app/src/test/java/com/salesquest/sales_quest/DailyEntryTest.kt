package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.ui.home.HomeViewModel
import com.salesquest.sales_quest.ui.home.validateDailyEntry
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 每日基础任务数据录入功能测试
 *
 * 覆盖:
 * - 新增数据 (输入 150/23/5 保存)
 * - 修改数据 (已有 150 改为 143)
 * - 历史数据 (过去日期录入/再次进入仍显示)
 * - 输入校验 (0 / 整数 / 负数 / 小数 / 字母 / 空值)
 * - 首页同步 (今日数据 / 本周统计 / 折线图)
 * - 数据持久化 (重启后不丢失)
 */
@RunWith(RobolectricTestRunner::class)
class DailyEntryTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private val todayKey get() = DateUtil.dateKey()
    private val yesterdayKey get() = DateUtil.dateKey(DateUtil.yesterdayStart())

    private suspend fun readToday() = Triple(
        db.settingDao().getInt(SettingsKeys.peopleSeen(todayKey)),
        db.settingDao().getInt(SettingsKeys.queries(todayKey)),
        db.settingDao().getInt(SettingsKeys.deals(todayKey))
    )

    // ================================================================
    // 新增数据
    // ================================================================

    @Test
    fun 新增数据_输入150_23_5保存成功() = runTest {
        assertNull(db.settingDao().get(SettingsKeys.peopleSeen(todayKey)))

        // 模拟 QuickActionSheet 保存逻辑 (今天走 quickActionService)
        AppContainer.quickActionService.setPeopleSeen(150)
        AppContainer.quickActionService.setQuery(23)
        AppContainer.quickActionService.setDeal(5)

        val data = readToday()
        assertEquals(150, data.first)
        assertEquals(23, data.second)
        assertEquals(5, data.third)
    }

    // ================================================================
    // 修改数据
    // ================================================================

    @Test
    fun 修改数据_已有150改为143() = runTest {
        AppContainer.quickActionService.setPeopleSeen(150)
        assertEquals(150, readToday().first)

        AppContainer.quickActionService.setPeopleSeen(143)
        assertEquals(143, readToday().first)

        // 累计同步变化
        assertEquals(143, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    // ================================================================
    // 历史数据
    // ================================================================

    @Test
    fun 历史数据_补录昨天并再次进入仍显示() = runTest {
        assertNull(db.settingDao().get(SettingsKeys.peopleSeen(yesterdayKey)))

        // 历史日期走 dailyStatsService (纯数据, 不发 XP)
        AppContainer.dailyStatsService.updateDailyStats(yesterdayKey, 135, 18, 3)

        val stats = AppContainer.dailyStatsService.getDailyStats(yesterdayKey)
        assertEquals(135, stats.peopleSeen)
        assertEquals(18, stats.queries)
        assertEquals(3, stats.deals)

        // 再次进入 (重新读取) 仍显示正确
        val again = AppContainer.dailyStatsService.getDailyStats(yesterdayKey)
        assertEquals(135, again.peopleSeen)
        assertEquals(18, again.queries)
        assertEquals(3, again.deals)
    }

    @Test
    fun 历史数据_修改已有记录() = runTest {
        AppContainer.dailyStatsService.updateDailyStats(yesterdayKey, 135, 18, 3)

        AppContainer.dailyStatsService.updateDailyStats(yesterdayKey, 142, 21, 4)

        val stats = AppContainer.dailyStatsService.getDailyStats(yesterdayKey)
        assertEquals(142, stats.peopleSeen)
        assertEquals(21, stats.queries)
        assertEquals(4, stats.deals)
    }

    @Test
    fun 历史数据修改_不产生XP() = runTest {
        val xpBefore = db.xpDao().getTotalXp()
        assertEquals(0, xpBefore)

        AppContainer.dailyStatsService.updateDailyStats(yesterdayKey, 142, 21, 4)

        assertEquals(xpBefore, db.xpDao().getTotalXp())
    }

    // ================================================================
    // 输入校验
    // ================================================================

    @Test
    fun 校验_空值不能保存() {
        assertNotNull(validateDailyEntry("", "0", "0"))
        assertNotNull(validateDailyEntry("0", "", "0"))
        assertNotNull(validateDailyEntry("0", "0", ""))
        assertNotNull(validateDailyEntry("  ", "0", "0"))
    }

    @Test
    fun 校验_负数被拒绝() {
        assertEquals("见人不能为负数", validateDailyEntry("-1", "0", "0"))
        assertEquals("查询不能为负数", validateDailyEntry("0", "-5", "0"))
        assertEquals("成交不能为负数", validateDailyEntry("0", "0", "-3"))
    }

    @Test
    fun 校验_小数被拒绝() {
        assertEquals("见人只能输入非负整数", validateDailyEntry("12.5", "0", "0"))
        assertEquals("查询只能输入非负整数", validateDailyEntry("0", "1.5", "0"))
        assertEquals("成交只能输入非负整数", validateDailyEntry("0", "0", "0.9"))
    }

    @Test
    fun 校验_字母被拒绝() {
        assertEquals("见人只能输入非负整数", validateDailyEntry("abc", "0", "0"))
        assertEquals("查询只能输入非负整数", validateDailyEntry("0", "x", "0"))
    }

    @Test
    fun 校验_0和正常整数合法() {
        assertNull(validateDailyEntry("0", "0", "0"))
        assertNull(validateDailyEntry("150", "23", "5"))
        assertNull(validateDailyEntry("0", "23", "5"))
    }

    // ================================================================
    // 首页同步
    // ================================================================

    @Test
    fun 首页同步_今日数据正确() = runTest {
        AppContainer.quickActionService.setPeopleSeen(150)
        AppContainer.quickActionService.setQuery(23)
        AppContainer.quickActionService.setDeal(5)

        val weekStats = HomeViewModel.buildWeekStats(db.settingDao().getAll())
        val today = weekStats.first { it.dateKey == todayKey }
        assertEquals(150, today.stats.peopleSeen)
        assertEquals(23, today.stats.queries)
        assertEquals(5, today.stats.deals)
    }

    @Test
    fun 首页同步_修改历史后本周统计与折线图更新() = runTest {
        val mon = DateUtil.weekDateKeys()[0]
        val tue = DateUtil.weekDateKeys()[1]

        AppContainer.dailyStatsService.updateDailyStats(mon, 150, 23, 5)
        AppContainer.dailyStatsService.updateDailyStats(tue, 132, 19, 3)

        var weekStats = HomeViewModel.buildWeekStats(db.settingDao().getAll())
        assertEquals(150, weekStats[0].stats.peopleSeen)
        assertEquals(132, weekStats[1].stats.peopleSeen)

        // 修改周二见人 132 -> 145
        AppContainer.dailyStatsService.updateDailyStats(tue, 145, 19, 3)

        weekStats = HomeViewModel.buildWeekStats(db.settingDao().getAll())
        assertEquals(145, weekStats[1].stats.peopleSeen)
        assertEquals(150, weekStats[0].stats.peopleSeen)

        // 本周累计正确
        assertEquals(295, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    // ================================================================
    // 数据持久化
    // ================================================================

    @Test
    fun 数据持久化_重启后不丢失() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = File(context.cacheDir, "daily_entry_test_${System.nanoTime()}.db")

        var db1 = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db1)
        AppContainer.quickActionService.setPeopleSeen(150)
        AppContainer.quickActionService.setQuery(23)
        AppContainer.quickActionService.setDeal(5)
        AppContainer.dailyStatsService.updateDailyStats(yesterdayKey, 135, 18, 3)
        db1.close()

        // 模拟重启
        db1 = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db1)

        val today = AppContainer.dailyStatsService.getDailyStats(todayKey)
        assertEquals(150, today.peopleSeen)
        assertEquals(23, today.queries)
        assertEquals(5, today.deals)

        val yesterday = AppContainer.dailyStatsService.getDailyStats(yesterdayKey)
        assertEquals(135, yesterday.peopleSeen)
        assertEquals(18, yesterday.queries)
        assertEquals(3, yesterday.deals)

        db1.close()
        dbFile.delete()
    }
}
