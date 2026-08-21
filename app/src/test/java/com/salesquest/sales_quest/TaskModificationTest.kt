package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.services.DailyTaskConfig
import com.salesquest.sales_quest.services.DailyTaskService
import com.salesquest.sales_quest.services.XpService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * P1 task target modification + streak + XP anti-repeat tests.
 *
 * Core invariant under test: after a day's config is changed (target raised or
 * lowered), XP that was already awarded for a completed task must NOT be
 * re-awarded, newly-completed tasks must be rewarded exactly once, the streak
 * counter may only advance once per day, and deal extra XP may only reward the
 * delta since the last award.
 *
 * Anti-repeat protection lives in [XpService]:
 *  - task XP: guarded by SettingsKeys.taskXp(taskId, dateKey) marker
 *  - deal extra XP: guarded by SettingsKeys.dealExtraXpAwarded(dateKey) count
 *  - daily completion / streak: guarded by SettingsKeys.dailyCompletion(dateKey) marker
 *
 * Targets are user-editable on the same day (DailyTaskService.setDayConfig
 * rebuilds task rows without locking), so these guards are the only line of
 * defense against double rewards.
 */
@RunWith(RobolectricTestRunner::class)
class TaskModificationTest {

    private lateinit var db: AppDatabase
    private lateinit var taskService: DailyTaskService
    private lateinit var xpService: XpService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskService = DailyTaskService(db)
        xpService = XpService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    /** Award XP for every task in a newly-completed batch (mirrors HomeViewModel flow). */
    private suspend fun awardFor(newlyCompleted: List<DailyTaskEntity>) {
        for (task in newlyCompleted) {
            xpService.awardTaskXp(task.taskId, task.xpReward)
        }
    }

    // ================================================================
    // Group A: task target modification reward consistency
    // ================================================================

    /**
     * TEST 1: 100 -> 150 (raise target after completion).
     *
     * Both tasks complete at meet=100/query=5 -> XP awarded -> target raised to
     * 150 -> meet no longer completed, query freshly rebuilt but XP marker
     * blocks re-award -> totalXp must stay the same.
     */
    @Test
    fun raiseTargetAfterCompletion_xpNotReAwarded() = runTest {
        // 1. Initial config: meetTarget=100, queryTarget=5 (both included).
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 100, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        // 2. Reach both targets: meet 100>=100, query 5>=5.
        xpService.setPeopleSeen(100)
        xpService.setQuery(5)

        // 3. Refresh + award XP for newly completed tasks (meet +100, query +80).
        val newlyCompleted = taskService.refreshTodayProgress()
        awardFor(newlyCompleted)

        val xpAfterFirstCompletion = db.statsDao().getStats()?.totalXp ?: 0
        assertTrue("first completion should award XP", xpAfterFirstCompletion > 0)

        // 4. Raise meet target 100 -> 150 (meet is no longer completed since 100<150).
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 150, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        // 5. Refresh again; meet is not completed, query is rebuilt but already has an XP marker.
        val newlyCompleted2 = taskService.refreshTodayProgress()

        // 6. Attempt to re-award XP for whatever is reported newly completed.
        awardFor(newlyCompleted2)

        // 7. totalXp must be unchanged (no re-award).
        val xpAfterModification = db.statsDao().getStats()?.totalXp ?: 0
        assertEquals(xpAfterFirstCompletion, xpAfterModification)
    }

    /**
     * TEST 2: 150 -> 80 (lower target, task becomes newly completed).
     *
     * meet=100 is NOT completed at target=150 (only query completes) -> lower
     * target to 80 -> meet becomes newly completed -> award exactly +100 once.
     * A second award attempt returns 0 and totalXp is unchanged.
     */
    @Test
    fun lowerTargetAfterCompletion_newlyCompletedAwardedOnce() = runTest {
        // 1. High target: meetTarget=150 (100<150, not completed), queryTarget=5 (5>=5, completed).
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 150, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        xpService.setPeopleSeen(100)
        xpService.setQuery(5)

        // 2. Refresh + award (only query newly completes: +80).
        val newlyCompleted = taskService.refreshTodayProgress()
        awardFor(newlyCompleted)

        val xpBeforeLowering = db.statsDao().getStats()?.totalXp ?: 0

        // 3. Lower meet target 150 -> 80; now 100>=80 so meet becomes completed.
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 80, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        // 4. Refresh: meet is newly completed (query is rebuilt-fresh but its XP marker blocks it).
        val newlyCompleted2 = taskService.refreshTodayProgress()
        awardFor(newlyCompleted2)

        // 5. totalXp increases by exactly the meet reward (100); query is not re-awarded.
        val xpAfterLowering = db.statsDao().getStats()?.totalXp ?: 0
        assertEquals(xpBeforeLowering + 100, xpAfterLowering)

        // 6. Second award attempt for the meet task returns 0; totalXp stays put.
        val reAwarded = xpService.awardTaskXp("task_meet", 100)
        assertEquals(0, reAwarded)
        assertEquals(xpAfterLowering, db.statsDao().getStats()?.totalXp ?: 0)
    }

    // ================================================================
    // Group B: streak behavior
    // ================================================================

    /**
     * TEST 3: all tasks completed -> streak advances by 1.
     */
    @Test
    fun allTasksCompleted_streakIncrementsOnce() = runTest {
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(
                meetTarget = 100,
                queryTarget = 5,
                includeMeet = true,
                includeQuery = true,
                includeDeal = false
            )
        )

        xpService.setPeopleSeen(100)
        xpService.setQuery(5)

        val triggered = xpService.onDailyTasksCompleted()
        assertTrue(triggered)

        val stats = db.statsDao().getStats()
        assertEquals(1, stats?.streakDays)
    }

    /**
     * TEST 4: partial completion -> checkAllTasksCompleted returns false
     * (so onDailyTasksCompleted must not be invoked).
     */
    @Test
    fun partialCompletion_allCompletedCheckReturnsFalse() = runTest {
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 100, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        xpService.setPeopleSeen(50) // 50 < 100 -> not completed
        xpService.setQuery(5)       // 5 >= 5   -> completed

        val allCompleted = taskService.checkAllTasksCompleted()
        assertFalse(allCompleted)
    }

    /**
     * TEST 5: calling onDailyTasksCompleted a second time the same day returns
     * false and the streak does not advance again.
     */
    @Test
    fun repeatDailyCompletionCheck_streakDoesNotIncrement() = runTest {
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 100, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        xpService.setPeopleSeen(100)
        xpService.setQuery(5)

        val first = xpService.onDailyTasksCompleted()
        assertTrue(first)
        assertEquals(1, db.statsDao().getStats()?.streakDays)

        val second = xpService.onDailyTasksCompleted()
        assertFalse(second)
        assertEquals(1, db.statsDao().getStats()?.streakDays)
    }

    // ================================================================
    // Group C: XP anti-repeat
    // ================================================================

    /**
     * TEST 6: the same task can only be rewarded once per day; the second call
     * returns 0 and totalXp is unchanged.
     */
    @Test
    fun sameTaskCannotBeRewardedTwice() = runTest {
        val first = xpService.awardTaskXp("task_meet", 100)
        assertEquals(100, first)

        val second = xpService.awardTaskXp("task_meet", 100)
        assertEquals(0, second)

        val stats = db.statsDao().getStats()
        assertEquals(100, stats?.totalXp)
    }

    /**
     * TEST 7: deal extra XP only rewards the delta since the last award, so
     * raising the deal count from 2 to 3 only grants 1*50 = 50 extra (total 150).
     */
    @Test
    fun dealIncreaseDoesNotReAwardHistoricalDealXp() = runTest {
        xpService.setDeal(2)
        val first = xpService.awardDealExtraXp(2)
        assertEquals(100, first) // 2 * 50

        xpService.setDeal(3) // increase by 1
        val second = xpService.awardDealExtraXp(3)
        assertEquals(50, second) // (3 - 2) * 50, only the delta

        val stats = db.statsDao().getStats()
        assertEquals(150, stats?.totalXp) // 100 + 50
    }
}
