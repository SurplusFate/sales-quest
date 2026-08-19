package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.DefaultTaskConfig
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.core.XpRewards
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.DailyTaskConfig
import com.salesquest.sales_quest.services.DailyTaskService
import com.salesquest.sales_quest.services.XpService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 对应 legacy/test/data_layer_test.dart:
 * P0 累计数据修复 / XP 事务一致性 / 首次默认任务配置 / 回归测试
 */
@RunWith(RobolectricTestRunner::class)
class DataLayerTest {

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

    // ================================================================
    // P0: 累计数据修复
    // 公式: 累计新值 = 累计旧值 - 当天旧值 + 当天新值
    // 测试场景: 100→80, 80→120, 120→120, 120→0
    // ================================================================

    @Test
    fun setPeopleSeen_100To80_累计应减少20() = runTest {
        xpService.setPeopleSeen(100)
        assertEquals(100, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(80)
        assertEquals(80, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    @Test
    fun setPeopleSeen_80To120_累计应增加40() = runTest {
        xpService.setPeopleSeen(80)
        assertEquals(80, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(120)
        assertEquals(120, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    @Test
    fun setPeopleSeen_120To120_累计不变() = runTest {
        xpService.setPeopleSeen(120)
        assertEquals(120, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(120)
        assertEquals(120, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    @Test
    fun setPeopleSeen_120To0_累计归零() = runTest {
        xpService.setPeopleSeen(120)
        assertEquals(120, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(0)
        assertEquals(0, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    @Test
    fun setPeopleSeen_连续变化_100到80到120到120到0() = runTest {
        xpService.setPeopleSeen(100)
        assertEquals(100, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(80)
        assertEquals(80, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(120)
        assertEquals(120, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(120)
        assertEquals(120, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))

        xpService.setPeopleSeen(0)
        assertEquals(0, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    @Test
    fun setQuery_10到5到15到15到0() = runTest {
        xpService.setQuery(10)
        assertEquals(10, db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES))

        xpService.setQuery(5)
        assertEquals(5, db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES))

        xpService.setQuery(15)
        assertEquals(15, db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES))

        xpService.setQuery(15)
        assertEquals(15, db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES))

        xpService.setQuery(0)
        assertEquals(0, db.settingDao().getInt(SettingsKeys.TOTAL_QUERIES))
    }

    @Test
    fun setDeal_3到1到5到5到0() = runTest {
        xpService.setDeal(3)
        assertEquals(3, db.settingDao().getInt(SettingsKeys.TOTAL_DEALS))

        xpService.setDeal(1)
        assertEquals(1, db.settingDao().getInt(SettingsKeys.TOTAL_DEALS))

        xpService.setDeal(5)
        assertEquals(5, db.settingDao().getInt(SettingsKeys.TOTAL_DEALS))

        xpService.setDeal(5)
        assertEquals(5, db.settingDao().getInt(SettingsKeys.TOTAL_DEALS))

        xpService.setDeal(0)
        assertEquals(0, db.settingDao().getInt(SettingsKeys.TOTAL_DEALS))
    }

    @Test
    fun 跨天独立计算_第一天100第二天50累计150() = runTest {
        val todayKey = DateUtil.dateKey()
        val yesterdayKey = DateUtil.dateKey(DateUtil.yesterdayStart())

        db.settingDao().setInt(SettingsKeys.peopleSeen(yesterdayKey), 100)
        db.settingDao().setInt(SettingsKeys.TOTAL_MEETS, 100)

        xpService.setPeopleSeen(50)

        assertEquals(50, db.settingDao().getInt(SettingsKeys.peopleSeen(todayKey)))
        assertEquals(150, db.settingDao().getInt(SettingsKeys.TOTAL_MEETS))
    }

    // ================================================================
    // P0: XP 事务一致性
    // ================================================================

    @Test
    fun awardTaskXp_同一任务同一天只能发放一次() = runTest {
        val taskId = "task_meet"
        val xpAmount = 100

        val first = xpService.awardTaskXp(taskId, xpAmount)
        assertEquals(100, first)

        val second = xpService.awardTaskXp(taskId, xpAmount)
        assertEquals(0, second)

        val stats = db.statsDao().getStats()
        assertEquals(100, stats?.totalXp)
    }

    @Test
    fun awardTaskXp_XP记录和领取标记和totalXp三者一致() = runTest {
        val taskId = "task_query"
        val xpAmount = 80

        xpService.awardTaskXp(taskId, xpAmount)

        val dk = DateUtil.dateKey()
        val hasXp = db.xpDao()
            .getXpForToday("daily", "TASK_$taskId", DateUtil.dayStart(), DateUtil.dayEnd())
            .isNotEmpty()
        assertEquals(true, hasXp)

        val marker = db.settingDao().get(SettingsKeys.taskXp(taskId, dk))
        assertEquals("1", marker)

        val stats = db.statsDao().getStats()
        assertEquals(80, stats?.totalXp)
    }

    @Test
    fun awardTaskXp_不同任务可以分别发放() = runTest {
        xpService.awardTaskXp("task_meet", 100)
        xpService.awardTaskXp("task_query", 80)

        val stats = db.statsDao().getStats()
        assertEquals(180, stats?.totalXp)
    }

    @Test
    fun awardDealExtraXp_相同数量不应重复发放() = runTest {
        xpService.setDeal(2)
        val first = xpService.awardDealExtraXp(2)
        assertEquals(2 * XpRewards.dealExtraXp, first)

        val second = xpService.awardDealExtraXp(2)
        assertEquals(0, second)

        val stats = db.statsDao().getStats()
        assertEquals(2 * XpRewards.dealExtraXp, stats?.totalXp)
    }

    @Test
    fun awardDealExtraXp_新增成交后才发放差额XP() = runTest {
        xpService.setDeal(1)
        xpService.awardDealExtraXp(1)

        xpService.setDeal(3)
        val awarded = xpService.awardDealExtraXp(3)
        assertEquals(2 * XpRewards.dealExtraXp, awarded)
    }

    @Test
    fun onDailyTasksCompleted_同一天重复触发不增加streak和XP() = runTest {
        val first = xpService.onDailyTasksCompleted()
        assertEquals(true, first)

        val stats1 = db.statsDao().getStats()
        val xpAfterFirst = stats1?.totalXp
        val streakAfterFirst = stats1?.streakDays

        val second = xpService.onDailyTasksCompleted()
        assertEquals(false, second)

        val stats2 = db.statsDao().getStats()
        assertEquals(xpAfterFirst, stats2?.totalXp)
        assertEquals(streakAfterFirst, stats2?.streakDays)
    }

    // ================================================================
    // P1: 首次默认任务配置 (全新数据库)
    // ================================================================

    @Test
    fun 全新数据库getDefaultConfig应返回推荐默认值() = runTest {
        val config = taskService.getDefaultConfig()

        assertEquals(100, config.meetTarget)
        assertEquals(5, config.queryTarget)
        assertEquals(1, config.dealTarget)
        assertEquals(true, config.includeMeet)
        assertEquals(true, config.includeQuery)
        assertEquals(false, config.includeDeal)
    }

    @Test
    fun 全新数据库getTodayConfig应返回推荐默认值() = runTest {
        val config = taskService.getTodayConfig()

        assertEquals(100, config.meetTarget)
        assertEquals(5, config.queryTarget)
        assertEquals(true, config.includeMeet)
        assertEquals(true, config.includeQuery)
        assertEquals(false, config.includeDeal)
    }

    @Test
    fun 全新数据库ensureTodayTasks应创建见人加查询两个任务() = runTest {
        taskService.ensureTodayTasks()

        val dk = DateUtil.dateKey()
        val tasks = db.taskDao().getByDate(dk)

        assertEquals(2, tasks.size)
        assertEquals(true, tasks.any { it.metric == "MEET" })
        assertEquals(true, tasks.any { it.metric == "QUERY" })
        assertEquals(false, tasks.any { it.metric == "DEAL" })
    }

    @Test
    fun 保存配置后getDefaultConfig应返回新值() = runTest {
        val config = DailyTaskConfig(
            meetTarget = 200,
            queryTarget = 10,
            dealTarget = 3,
            includeMeet = true,
            includeQuery = true,
            includeDeal = true
        )

        taskService.saveDefaultConfig(config)
        val loaded = taskService.getDefaultConfig()

        assertEquals(200, loaded.meetTarget)
        assertEquals(10, loaded.queryTarget)
        assertEquals(3, loaded.dealTarget)
        assertEquals(true, loaded.includeMeet)
        assertEquals(true, loaded.includeQuery)
        assertEquals(true, loaded.includeDeal)
    }

    @Test
    fun 保存配置后includeDeal为false能正确读取() = runTest {
        val config = DailyTaskConfig(
            meetTarget = 100,
            queryTarget = 5,
            dealTarget = 1,
            includeMeet = true,
            includeQuery = true,
            includeDeal = false
        )

        taskService.saveDefaultConfig(config)
        val loaded = taskService.getDefaultConfig()

        assertEquals(false, loaded.includeDeal)
        assertEquals(true, loaded.includeMeet)
        assertEquals(true, loaded.includeQuery)
    }

    // ================================================================
    // 回归测试
    // ================================================================

    @Test
    fun dailyTaskConfig默认构造仍为推荐值() {
        val config = DailyTaskConfig()
        assertEquals(100, config.meetTarget)
        assertEquals(5, config.queryTarget)
        assertEquals(true, config.includeMeet)
        assertEquals(true, config.includeQuery)
        assertEquals(false, config.includeDeal)
    }

    @Test
    fun 任务不再锁定_允许当天修改目标() = runTest {
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 100)
        )
        taskService.lockTodayTasks()

        // P1: 任务不再锁定, 允许修改
        assertEquals(false, taskService.isTodayLocked())

        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 1)
        )
        val config = taskService.getTodayConfig()
        assertEquals(1, config.meetTarget)
    }

    @Test
    fun 成交不参与时见人加查询达标即完成() = runTest {
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig()
        )

        xpService.setPeopleSeen(100)
        xpService.setQuery(5)

        val allCompleted = taskService.checkAllTasksCompleted()
        assertEquals(true, allCompleted)
    }

    @Test
    fun 连续作战首次完成streak为1() = runTest {
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig()
        )

        xpService.setPeopleSeen(100)
        xpService.setQuery(5)

        val triggered = xpService.onDailyTasksCompleted()
        assertEquals(true, triggered)

        val stats = db.statsDao().getStats()
        assertEquals(1, stats?.streakDays)
    }

    // ================================================================
    // P1: 修改任务目标后防重复奖励测试
    // 场景: 完成任务→获得XP→修改目标→再次达到完成条件→不得重复获得XP
    // ================================================================

    @Test
    fun 修改任务目标后_不得重复获得任务完成XP() = runTest {
        // 1. 设置初始任务: 查询目标 10
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 100, queryTarget = 10, includeMeet = true, includeQuery = true)
        )

        // 2. 完成查询任务 (达到 10)
        xpService.setPeopleSeen(100)
        xpService.setQuery(10)

        // 3. 刷新进度, 发放 XP
        val newlyCompleted = taskService.refreshTodayProgress()
        for (task in newlyCompleted) {
            xpService.awardTaskXp(task.taskId, task.xpReward)
        }

        // 记录当前 XP
        val xpAfterFirstCompletion = db.statsDao().getStats()?.totalXp ?: 0
        assertTrue("首次完成应获得 XP", xpAfterFirstCompletion > 0)

        // 4. 修改查询目标为 5 (降低目标)
        taskService.setDayConfig(
            System.currentTimeMillis(),
            DailyTaskConfig(meetTarget = 100, queryTarget = 5, includeMeet = true, includeQuery = true)
        )

        // 5. 再次刷新进度 (目标 5, 已完成 10 → 仍然完成)
        val newlyCompleted2 = taskService.refreshTodayProgress()

        // 6. 尝试再次发放 XP
        for (task in newlyCompleted2) {
            xpService.awardTaskXp(task.taskId, task.xpReward)
        }

        // 7. XP 不应增加 (防重复)
        val xpAfterModification = db.statsDao().getStats()?.totalXp ?: 0
        assertEquals("修改目标后不得重复获得 XP", xpAfterFirstCompletion, xpAfterModification)
    }
}
