package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.DailyStatsService
import com.salesquest.sales_quest.ui.home.HomeViewModel
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 首页「本周战绩」折线图数据场景测试
 *
 * 覆盖:
 * 1. 周一至周六日期计算
 * 2. 跨周日期计算
 * 3. 有数据日期读取
 * 4. 无数据日期显示 0
 * 5. 当前周未来日期显示 0
 * 6. 首页数据与 DailyStatsService 数据一致
 */
@RunWith(RobolectricTestRunner::class)
class WeekStatsTest {

    private lateinit var db: AppDatabase
    private lateinit var statsService: DailyStatsService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db)
        statsService = DailyStatsService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** 本地时区某日 0 点时间戳 */
    private fun localMillis(y: Int, m: Int, d: Int): Long {
        val c = Calendar.getInstance()
        c.set(y, m - 1, d, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    // ================================================================
    // 1. 周一至周六日期计算
    // ================================================================

    @Test
    fun 周二所在周_返回周一至周六六天() {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 18))
        assertEquals(
            listOf("2026-08-17", "2026-08-18", "2026-08-19", "2026-08-20", "2026-08-21", "2026-08-22"),
            keys
        )
        assertEquals(6, keys.size)
    }

    @Test
    fun 周一所在周_周一起始() {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 17))
        assertEquals("2026-08-17", keys.first())
        assertEquals("2026-08-22", keys.last())
    }

    @Test
    fun 周六所在周_六天不含周日() {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 29))
        assertEquals("2026-08-24", keys.first())
        assertEquals("2026-08-29", keys.last())
    }

    @Test
    fun 周几名称正确() {
        assertEquals("周一", DateUtil.weekdayName("2026-08-17"))
        assertEquals("周二", DateUtil.weekdayName("2026-08-18"))
        assertEquals("周三", DateUtil.weekdayName("2026-08-19"))
        assertEquals("周六", DateUtil.weekdayName("2026-08-22"))
        assertEquals("周日", DateUtil.weekdayName("2026-08-23"))
    }

    @Test
    fun 月日标签正确() {
        assertEquals("8月18日", DateUtil.monthDayLabel("2026-08-18"))
        assertEquals("12月31日", DateUtil.monthDayLabel("2026-12-31"))
    }

    // ================================================================
    // 2. 跨周日期计算
    // ================================================================

    @Test
    fun 周日归属上周_周一为上周一() {
        assertEquals(
            localMillis(2026, 8, 17),
            DateUtil.mondayStart(localMillis(2026, 8, 23))
        )
    }

    @Test
    fun 跨月_周六所在周周一在上月() {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 1))
        assertEquals("2026-07-27", keys.first())
        assertEquals("2026-08-01", keys.last())
    }

    @Test
    fun 跨年_一月初所在周周一在去年() {
        val keys = DateUtil.weekDateKeys(localMillis(2027, 1, 2))
        // 2027-01-02 是周六, 周一为 2026-12-28
        assertEquals("2026-12-28", keys.first())
        assertEquals("2027-01-02", keys.last())
    }

    // ================================================================
    // 3. 有数据日期读取
    // ================================================================

    @Test
    fun 有数据日期_读取到每日数据() = runTest {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 18))
        statsService.updateDailyStats(keys[0], 20, 10, 4)
        statsService.updateDailyStats(keys[1], 35, 18, 6)

        val mon = statsService.getDailyStats(keys[0])
        assertEquals(20, mon.peopleSeen)
        assertEquals(10, mon.queries)
        assertEquals(4, mon.deals)

        val tue = statsService.getDailyStats(keys[1])
        assertEquals(35, tue.peopleSeen)
        assertEquals(18, tue.queries)
        assertEquals(6, tue.deals)
    }

    // ================================================================
    // 4. 无数据日期显示 0
    // ================================================================

    @Test
    fun 无数据日期_三项均为0但日期保留() = runTest {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 18))
        statsService.updateDailyStats(keys[0], 20, 10, 4)

        val empty = statsService.getDailyStats(keys[2])
        assertEquals(0, empty.peopleSeen)
        assertEquals(0, empty.queries)
        assertEquals(0, empty.deals)

        // 6 天全部保留在横轴
        assertEquals(6, keys.size)
    }

    // ================================================================
    // 5. 当前周未来日期显示 0
    // ================================================================

    @Test
    fun 未来日期_无历史数据_显示0() = runTest {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 18))
        // 只录入今天(周二)数据, 周三~周六均为未来
        statsService.updateDailyStats(keys[1], 35, 18, 6)

        val future = statsService.getDailyStats(keys[5])
        assertEquals(0, future.peopleSeen)
        assertEquals(0, future.queries)
        assertEquals(0, future.deals)
    }

    // ================================================================
    // 6. 首页数据与 DailyStatsService 数据一致
    // ================================================================

    @Test
    fun 首页本周战绩与DailyStatsService数据一致() = runTest {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 18))
        statsService.updateDailyStats(keys[0], 20, 10, 4)
        statsService.updateDailyStats(keys[1], 35, 18, 6)
        statsService.updateDailyStats(keys[3], 42, 21, 7)

        val weekStats = HomeViewModel.buildWeekStats(db.settingDao().getAll())

        assertEquals(6, weekStats.size)
        // 周一
        assertEquals("周一", weekStats[0].weekday)
        assertEquals(20, weekStats[0].stats.peopleSeen)
        assertEquals(10, weekStats[0].stats.queries)
        assertEquals(4, weekStats[0].stats.deals)
        // 周二
        assertEquals(35, weekStats[1].stats.peopleSeen)
        // 周三 (无数据)
        assertEquals(0, weekStats[2].stats.peopleSeen)
        // 周五 (有数据)
        assertEquals(42, weekStats[3].stats.peopleSeen)
        // 周六 (未来)
        assertEquals(0, weekStats[5].stats.peopleSeen)

        // 与 DailyStatsService 逐日一致
        for (i in keys.indices) {
            val expected = statsService.getDailyStats(keys[i])
            val actual = weekStats[i].stats
            assertEquals(expected, actual)
        }
    }

    @Test
    fun 首页修改数据后本周战绩自动刷新() = runTest {
        val keys = DateUtil.weekDateKeys(localMillis(2026, 8, 18))
        statsService.updateDailyStats(keys[0], 10, 5, 2)

        val weekStats = HomeViewModel.buildWeekStats(db.settingDao().getAll())
        assertEquals(10, weekStats[0].stats.peopleSeen)

        // 修改周一数据 (模拟数据分析页改历史数据)
        statsService.updateDailyStats(keys[0], 88, 30, 9)

        val updated = HomeViewModel.buildWeekStats(db.settingDao().getAll())
        assertEquals(88, updated[0].stats.peopleSeen)
        assertEquals(30, updated[0].stats.queries)
        assertEquals(9, updated[0].stats.deals)
    }
}
