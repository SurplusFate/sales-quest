package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.DailyStatsService
import com.salesquest.sales_quest.services.XpService
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 历史日期数据查看/录入/修改 核心场景测试
 *
 * 8 个指定场景:
 * 1. 修改今天 - 累计跟随变化
 * 2. 修改昨天 - 历史日期修改后累计正确
 * 3. 重复修改 - 累计不叠加, 始终等于当前值之和
 * 4. 补录历史 - 空白历史日期可直接录入
 * 5. 非法数据拦截 - 负数/查询>见人/成交>查询
 * 6. 历史修改不产生 XP
 * 7. 重启持久性 - 数据落盘后重开仍在
 * 8. 累计 = 逐日求和
 */
@RunWith(RobolectricTestRunner::class)
class HistoryStatsTest {

    private lateinit var db: AppDatabase
    private lateinit var statsService: DailyStatsService
    private lateinit var xpService: XpService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        statsService = DailyStatsService(db)
        xpService = XpService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun readDaily(dateKey: String) = statsService.getDailyStats(dateKey)

    private suspend fun readTotal() = Triple(
        db.settingDao().getInt(SettingsKeys.TOTAL_MEETS),
        db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES),
        db.settingDao().getInt(SettingsKeys.TOTAL_DEALS)
    )

    // ================================================================
    // 场景 1: 修改今天
    // ================================================================

    @Test
    fun 修改今天_累计跟随变化() = runTest {
        val todayKey = DateUtil.dateKey()
        assertNull(db.settingDao().get(SettingsKeys.TOTAL_MEETS))

        statsService.updateDailyStats(todayKey, 10, 5, 2)

        val stats = readDaily(todayKey)
        assertEquals(10, stats.peopleSeen)
        assertEquals(5, stats.queries)
        assertEquals(2, stats.deals)

        val total = readTotal()
        assertEquals(10, total.first)
        assertEquals(5, total.second)
        assertEquals(2, total.third)
    }

    // ================================================================
    // 场景 2: 修改昨天
    // ================================================================

    @Test
    fun 修改昨天_累计包含历史日期() = runTest {
        val todayKey = DateUtil.dateKey()
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        statsService.updateDailyStats(yesterdayKey, 7, 4, 1)
        statsService.updateDailyStats(todayKey, 3, 2, 1)

        val total = readTotal()
        assertEquals(10, total.first)
        assertEquals(6, total.second)
        assertEquals(2, total.third)
    }

    // ================================================================
    // 场景 3: 重复修改 - 累计不叠加
    // ================================================================

    @Test
    fun 重复修改同一历史日期_累计等于最终值不叠加() = runTest {
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        statsService.updateDailyStats(yesterdayKey, 5, 3, 1)
        statsService.updateDailyStats(yesterdayKey, 8, 6, 2)
        statsService.updateDailyStats(yesterdayKey, 4, 4, 4)

        val total = readTotal()
        assertEquals(4, total.first)
        assertEquals(4, total.second)
        assertEquals(4, total.third)
    }

    @Test
    fun 跨天多次修改_累计为各天当前值之和() = runTest {
        val todayKey = DateUtil.dateKey()
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        statsService.updateDailyStats(yesterdayKey, 20, 10, 5)
        statsService.updateDailyStats(yesterdayKey, 30, 20, 10)
        statsService.updateDailyStats(todayKey, 5, 3, 1)
        statsService.updateDailyStats(todayKey, 10, 5, 2)

        val total = readTotal()
        assertEquals(40, total.first)
        assertEquals(25, total.second)
        assertEquals(12, total.third)
    }

    // ================================================================
    // 场景 4: 补录历史
    // ================================================================

    @Test
    fun 补录三天前空白日期_累计正确() = runTest {
        val threeDaysAgoKey = DateUtil.dateKey(DateUtil.yesterdayStart(DateUtil.yesterdayStart(DateUtil.yesterdayStart())))

        statsService.updateDailyStats(threeDaysAgoKey, 6, 4, 2)

        val stats = readDaily(threeDaysAgoKey)
        assertEquals(6, stats.peopleSeen)
        assertEquals(4, stats.queries)
        assertEquals(2, stats.deals)

        val total = readTotal()
        assertEquals(6, total.first)
        assertEquals(4, total.second)
        assertEquals(2, total.third)
    }

    @Test
    fun 补录后与已有数据并存() = runTest {
        val todayKey = DateUtil.dateKey()
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        statsService.updateDailyStats(todayKey, 3, 2, 1)
        statsService.updateDailyStats(yesterdayKey, 1, 1, 1)

        val total = readTotal()
        assertEquals(4, total.first)
        assertEquals(3, total.second)
        assertEquals(2, total.third)
    }

    // ================================================================
    // 场景 5: 非法数据拦截
    // ================================================================

    @Test
    fun 负数_全部指标拦截() = runTest {
        val todayKey = DateUtil.dateKey()
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { statsService.updateDailyStats(todayKey, -1, 0, 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { statsService.updateDailyStats(todayKey, 0, -1, 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { statsService.updateDailyStats(todayKey, 0, 0, -1) }
        }
        // 数据未被写入
        assertNull(db.settingDao().get(SettingsKeys.peopleSeen(todayKey)))
        assertNull(db.settingDao().get(SettingsKeys.queries(todayKey)))
        assertNull(db.settingDao().get(SettingsKeys.deals(todayKey)))
    }

    @Test
    fun 查询数超过见人数_拦截() = runTest {
        val todayKey = DateUtil.dateKey()
        val ex = assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { statsService.updateDailyStats(todayKey, 5, 6, 1) }
        }
        assertEquals("查询数不能超过见人数", ex.message)
    }

    @Test
    fun 成交数超过查询数_拦截() = runTest {
        val todayKey = DateUtil.dateKey()
        val ex = assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { statsService.updateDailyStats(todayKey, 10, 5, 6) }
        }
        assertEquals("成交数不能超过查询数", ex.message)
    }

    @Test
    fun 非法值_不写入不破坏累计() = runTest {
        val todayKey = DateUtil.dateKey()
        statsService.updateDailyStats(todayKey, 10, 5, 2)
        val before = readTotal()

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { statsService.updateDailyStats(todayKey, 0, 0, -1) }
        }

        val after = readTotal()
        assertEquals(before, after)
        val stats = readDaily(todayKey)
        assertEquals(10, stats.peopleSeen)
        assertEquals(5, stats.queries)
        assertEquals(2, stats.deals)
    }

    @Test
    fun 全部为0_合法() = runTest {
        val todayKey = DateUtil.dateKey()
        statsService.updateDailyStats(todayKey, 0, 0, 0)
        val total = readTotal()
        assertEquals(0, total.first)
        assertEquals(0, total.second)
        assertEquals(0, total.third)
    }

    // ================================================================
    // 场景 6: 历史修改不产生 XP
    // ================================================================

    @Test
    fun 修改历史日期_不产生XP() = runTest {
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        statsService.updateDailyStats(yesterdayKey, 8, 5, 2)

        val xpTotal = db.xpDao().getTotalXp()
        assertEquals("历史修改不应产生任何 XP", 0, xpTotal)
        assertNull(db.statsDao().getStats())
    }

    @Test
    fun 频繁修改历史_累计变化但XP始终不变() = runTest {
        val todayKey = DateUtil.dateKey()
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        // 今天正常记录产生 XP (模拟 QuickActionService 链路的最终效果)
        xpService.setPeopleSeen(5)
        xpService.awardTaskXp("task_meet", 100)
        val xpAfterToday = db.statsDao().getStats()!!.totalXp

        // 反复修改历史日期, XP 不应有任何变化
        statsService.updateDailyStats(yesterdayKey, 1, 1, 1)
        statsService.updateDailyStats(yesterdayKey, 20, 10, 5)
        statsService.updateDailyStats(yesterdayKey, 50, 30, 10)
        statsService.updateDailyStats(yesterdayKey, 0, 0, 0)

        val xpAfterHistoryEdits = db.statsDao().getStats()!!.totalXp
        assertEquals(xpAfterToday, xpAfterHistoryEdits)

        // 累计数据随历史修改正确变化
        val total = readTotal()
        assertEquals(5, total.first)
        assertEquals(0, total.second)
        assertEquals(0, total.third)
    }

    // ================================================================
    // 场景 7: 重启持久性
    // ================================================================

    @Test
    fun 数据落盘_重新打开后历史数据仍在() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = File(context.cacheDir, "history_test_${System.nanoTime()}.db")

        var db1 = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        var service1 = DailyStatsService(db1)
        val pastKey = "2026-07-15"
        service1.updateDailyStats(pastKey, 12, 8, 3)
        db1.close()

        // 重新打开 (模拟重启)
        db1 = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        service1 = DailyStatsService(db1)

        val stats = service1.getDailyStats(pastKey)
        assertEquals(12, stats.peopleSeen)
        assertEquals(8, stats.queries)
        assertEquals(3, stats.deals)

        val total = Triple(
            db1.settingDao().getInt(SettingsKeys.TOTAL_MEETS),
            db1.settingDao().getInt(SettingsKeys.TOTAL_QUERIES),
            db1.settingDao().getInt(SettingsKeys.TOTAL_DEALS)
        )
        assertEquals(12, total.first)
        assertEquals(8, total.second)
        assertEquals(3, total.third)
        db1.close()
        dbFile.delete()
    }

    // ================================================================
    // 场景 8: 累计 = 逐日求和
    // ================================================================

    @Test
    fun 累计等于逐日求和() = runTest {
        val todayKey = DateUtil.dateKey()
        val y1 = DateUtil.dateKey(DateUtil.yesterdayStart())
        val y2 = DateUtil.dateKey(DateUtil.yesterdayStart(DateUtil.yesterdayStart()))

        statsService.updateDailyStats(y2, 2, 1, 0)
        statsService.updateDailyStats(y1, 5, 3, 1)
        statsService.updateDailyStats(todayKey, 3, 2, 1)

        val total = readTotal()
        assertEquals(10, total.first)
        assertEquals(6, total.second)
        assertEquals(2, total.third)
    }

    @Test
    fun 清零某天_累计同步减少() = runTest {
        val todayKey = DateUtil.dateKey()
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        statsService.updateDailyStats(yesterdayKey, 10, 5, 2)
        statsService.updateDailyStats(todayKey, 5, 3, 1)
        assertEquals(15, readTotal().first)

        statsService.updateDailyStats(yesterdayKey, 0, 0, 0)
        val total = readTotal()
        assertEquals(5, total.first)
        assertEquals(3, total.second)
        assertEquals(1, total.third)
    }
}
