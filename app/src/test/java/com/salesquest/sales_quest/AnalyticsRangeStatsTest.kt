package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.ui.data.AnalyticsViewModel
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 数据分析页「本周 / 本月」统计逻辑测试
 *
 * 覆盖:
 * 1. DateUtil.monthStart 月份起始计算
 * 2. DateUtil.dateKeysBetween 日期区间生成 (含端点)
 * 3. AnalyticsViewModel.sumRange 区间累计 (含空数据 / 无数据日期 / 跨月)
 */
@RunWith(RobolectricTestRunner::class)
class AnalyticsRangeStatsTest {

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

    /** 本地时区某日 0 点时间戳 */
    private fun localMillis(y: Int, m: Int, d: Int): Long {
        val c = Calendar.getInstance()
        c.set(y, m - 1, d, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    // ================================================================
    // DateUtil.monthStart
    // ================================================================

    @Test
    fun 月中某日_月份起始为当月1号0点() {
        assertEquals(localMillis(2026, 8, 1), DateUtil.monthStart(localMillis(2026, 8, 18)))
    }

    @Test
    fun 月末某日_月份起始为当月1号() {
        assertEquals(localMillis(2026, 8, 1), DateUtil.monthStart(localMillis(2026, 8, 31)))
    }

    @Test
    fun 一号本身_月份起始即当天() {
        assertEquals(localMillis(2026, 9, 1), DateUtil.monthStart(localMillis(2026, 9, 1)))
    }

    // ================================================================
    // DateUtil.dateKeysBetween
    // ================================================================

    @Test
    fun 日期区间_含端点生成全部日期() {
        assertEquals(
            listOf("2026-08-01", "2026-08-02", "2026-08-03"),
            DateUtil.dateKeysBetween(localMillis(2026, 8, 1), localMillis(2026, 8, 3))
        )
    }

    @Test
    fun 起始等于结束_仅生成一天() {
        assertEquals(
            listOf("2026-08-01"),
            DateUtil.dateKeysBetween(localMillis(2026, 8, 1), localMillis(2026, 8, 1))
        )
    }

    @Test
    fun 跨月区间_生成跨月日期() {
        assertEquals(
            listOf("2026-07-31", "2026-08-01", "2026-08-02"),
            DateUtil.dateKeysBetween(localMillis(2026, 7, 31), localMillis(2026, 8, 2))
        )
    }

    // ================================================================
    // AnalyticsViewModel.sumRange
    // ================================================================

    @Test
    fun 区间累计_汇总每日明细() = runTest {
        db.settingDao().setInt(SettingsKeys.peopleSeen("2026-08-17"), 20)
        db.settingDao().setInt(SettingsKeys.queries("2026-08-17"), 10)
        db.settingDao().setInt(SettingsKeys.deals("2026-08-17"), 4)
        db.settingDao().setInt(SettingsKeys.peopleSeen("2026-08-18"), 5)
        db.settingDao().setInt(SettingsKeys.queries("2026-08-18"), 2)
        db.settingDao().setInt(SettingsKeys.deals("2026-08-18"), 1)

        val stats = AnalyticsViewModel.sumRange(
            db.settingDao().getAll(),
            listOf("2026-08-17", "2026-08-18")
        )
        assertEquals(25, stats.peopleSeen)
        assertEquals(12, stats.queries)
        assertEquals(5, stats.deals)
    }

    @Test
    fun 区间内无数据日期_计为0不影响合计() = runTest {
        db.settingDao().setInt(SettingsKeys.peopleSeen("2026-08-17"), 20)
        db.settingDao().setInt(SettingsKeys.peopleSeen("2026-08-19"), 30)

        val stats = AnalyticsViewModel.sumRange(
            db.settingDao().getAll(),
            listOf("2026-08-17", "2026-08-18", "2026-08-19")
        )
        assertEquals(50, stats.peopleSeen)
        assertEquals(0, stats.queries)
        assertEquals(0, stats.deals)
    }

    @Test
    fun 无任何数据_汇总全为0() {
        val stats = AnalyticsViewModel.sumRange(
            emptyList(),
            listOf("2026-08-17", "2026-08-18")
        )
        assertEquals(0, stats.peopleSeen)
        assertEquals(0, stats.queries)
        assertEquals(0, stats.deals)
    }

    @Test
    fun 跨月区间累计正确() = runTest {
        db.settingDao().setInt(SettingsKeys.peopleSeen("2026-07-31"), 10)
        db.settingDao().setInt(SettingsKeys.deals("2026-08-01"), 3)

        val stats = AnalyticsViewModel.sumRange(
            db.settingDao().getAll(),
            listOf("2026-07-31", "2026-08-01")
        )
        assertEquals(10, stats.peopleSeen)
        assertEquals(0, stats.queries)
        assertEquals(3, stats.deals)
    }

    @Test
    fun 非当日明细键_不参与汇总() = runTest {
        db.settingDao().setInt(SettingsKeys.peopleSeen("2026-08-17"), 20)
        db.settingDao().setInt(SettingsKeys.TOTAL_MEETS, 999)

        val stats = AnalyticsViewModel.sumRange(
            db.settingDao().getAll(),
            listOf("2026-08-17", "2026-08-18")
        )
        assertEquals(20, stats.peopleSeen)
    }
}
