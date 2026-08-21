package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.DailyStatsService
import java.util.Calendar
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Date boundary consistency tests.
 *
 * Verifies that DateUtil's date handling never records data to the wrong date at the
 * critical boundaries of a day (00:00 / 23:59 / cross-midnight). All timestamps are
 * created with java.util.Calendar in the device's local timezone, matching how
 * DateUtil.dateKey derives its "yyyy-MM-dd" key. Uses Robolectric + Room in-memory DB.
 */
@RunWith(RobolectricTestRunner::class)
class DateBoundaryTest {

    private lateinit var db: AppDatabase
    private lateinit var dailyStatsService: DailyStatsService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dailyStatsService = DailyStatsService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Builds a timestamp for "today at the given hour/minute" (seconds & millis zeroed). */
    private fun todayAt(hour: Int, minute: Int = 0): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hour)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    // ================================================================
    // TEST 1: 23:59 - data recorded to today's date
    // ================================================================
    @Test
    fun `data at 2359 is recorded to today date`() = runTest {
        // Calendar for today at 23:59
        val time2359 = todayAt(23, 59)
        val dateKey = DateUtil.dateKey(time2359)

        // Write people_seen = 10 using that dateKey
        dailyStatsService.updateDailyMetric(dateKey, "MEET", 10)

        // Read back using the same dateKey (storage layer)
        assertEquals(10, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        // Read back via the service (BattleStats)
        val stats = dailyStatsService.getDailyStats(dateKey)
        assertEquals(10, stats.peopleSeen)
    }

    // ================================================================
    // TEST 2: 00:00 - data recorded to the next day, not the previous day
    // Midnight (new day) and the 23:59 one minute earlier (previous day)
    // are adjacent in time but must map to different date keys.
    // ================================================================
    @Test
    fun `data at 0000 is recorded to next day not previous day`() = runTest {
        // Calendar for today at 00:00 (midnight = start of the new day)
        val midnight = Calendar.getInstance()
        midnight.set(Calendar.HOUR_OF_DAY, 0)
        midnight.set(Calendar.MINUTE, 0)
        midnight.set(Calendar.SECOND, 0)
        midnight.set(Calendar.MILLISECOND, 0)
        val midnightKey = DateUtil.dateKey(midnight.timeInMillis)

        // 23:59 of the PREVIOUS day: one minute before midnight (the boundary moment)
        val prevDay2359 = (midnight.clone() as Calendar)
        prevDay2359.add(Calendar.MINUTE, -1)
        val prevDayKey = DateUtil.dateKey(prevDay2359.timeInMillis)

        // The two moments are only 1 minute apart, but must belong to different days
        assertNotEquals(midnightKey, prevDayKey)

        // people_seen = 5 recorded at midnight (the new day)
        dailyStatsService.updateDailyMetric(midnightKey, "MEET", 5)
        // people_seen = 10 recorded at 23:59 (which is actually the PREVIOUS day)
        dailyStatsService.updateDailyMetric(prevDayKey, "MEET", 10)

        // Both stored under separate keys, no bleed-over between days
        assertEquals(5, db.settingDao().getInt(SettingsKeys.peopleSeen(midnightKey)))
        assertEquals(10, db.settingDao().getInt(SettingsKeys.peopleSeen(prevDayKey)))

        val midnightStats = dailyStatsService.getDailyStats(midnightKey)
        val prevDayStats = dailyStatsService.getDailyStats(prevDayKey)
        assertEquals(5, midnightStats.peopleSeen)
        assertEquals(10, prevDayStats.peopleSeen)
    }

    // ================================================================
    // TEST 3: dayStart / dayEnd consistency
    // ================================================================
    @Test
    fun `dayStart and dayEnd bracket entire day`() = runTest {
        val noon = todayAt(12, 0)
        val start = DateUtil.dayStart(noon)
        val end = DateUtil.dayEnd(noon)

        assertTrue("dayStart should be before noon", start < noon)
        assertTrue("noon should be before dayEnd", noon < end)
        // Exactly 24 hours between dayStart and dayEnd
        assertEquals(24L * 60 * 60 * 1000, end - start)
    }

    // ================================================================
    // TEST 4: dateKey consistency - same day returns the same key
    // ================================================================
    @Test
    fun `dateKey is consistent across same day`() = runTest {
        // Several timestamps during the same day, all built with java.util.Calendar
        val times = listOf(
            todayAt(0, 0),   // 00:00
            todayAt(6, 0),   // 06:00
            todayAt(12, 0),  // 12:00
            todayAt(18, 0),  // 18:00
            todayAt(23, 59)  // 23:59
        )

        val keys = times.map { DateUtil.dateKey(it) }
        val first = keys.first()
        for (key in keys) {
            assertEquals(first, key)
        }
    }

    // ================================================================
    // TEST 5: Cross-midnight data separation
    // 23:59 on day 1 vs 00:01 on day 2 must be isolated by dateKey.
    // ================================================================
    @Test
    fun `cross midnight data is separated by dateKey`() = runTest {
        // Day 1 at 23:59
        val day1End = Calendar.getInstance()
        day1End.set(Calendar.HOUR_OF_DAY, 23)
        day1End.set(Calendar.MINUTE, 59)
        day1End.set(Calendar.SECOND, 0)
        day1End.set(Calendar.MILLISECOND, 0)
        val day1Key = DateUtil.dateKey(day1End.timeInMillis)

        // Day 2 at 00:01: 23:59 + 2 minutes crosses into the next day
        val day2Start = (day1End.clone() as Calendar)
        day2Start.add(Calendar.MINUTE, 2)
        val day2Key = DateUtil.dateKey(day2Start.timeInMillis)

        // The two records must live under different date keys
        assertNotEquals(day1Key, day2Key)

        // Record 7 at 23:59 on day 1
        dailyStatsService.updateDailyMetric(day1Key, "MEET", 7)
        // Record 9 at 00:01 on day 2
        dailyStatsService.updateDailyMetric(day2Key, "MEET", 9)

        // Both can be read back independently, no cross-contamination
        assertEquals(7, db.settingDao().getInt(SettingsKeys.peopleSeen(day1Key)))
        assertEquals(9, db.settingDao().getInt(SettingsKeys.peopleSeen(day2Key)))

        val day1Stats = dailyStatsService.getDailyStats(day1Key)
        val day2Stats = dailyStatsService.getDailyStats(day2Key)
        assertEquals(7, day1Stats.peopleSeen)
        assertEquals(9, day2Stats.peopleSeen)
    }
}
