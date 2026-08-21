package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.DefaultTaskConfig
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.DailyTaskConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 基础任务配置修改后首页目标实时同步测试
 *
 * 场景: 100 → 150 → 200, 修改后:
 * - 配置 Flow 立即发出新值
 * - 任务表 target 同步为 150
 * - 首页读取的实际生效配置与任务行 target 一致
 */
@RunWith(RobolectricTestRunner::class)
class HomeConfigSyncTest {

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

    private fun cfg(
        meet: Int = DefaultTaskConfig.recommendedMeetTarget,
        query: Int = DefaultTaskConfig.recommendedQueryTarget,
        deal: Int = DefaultTaskConfig.recommendedDealTarget
    ) = DailyTaskConfig(
        meetTarget = meet,
        queryTarget = query,
        dealTarget = deal,
        includeMeet = true,
        includeQuery = true,
        includeDeal = false
    )

    @Test
    fun 修改配置后任务表target同步更新() = runTest {
        val taskService = AppContainer.dailyTaskService

        // 初始配置 100, 建立今日任务
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 100))
        val today = com.salesquest.sales_quest.data.DateUtil.dateKey()
        val meetTaskBefore = db.taskDao().getByDate(today).first { it.metric == "MEET" }
        assertEquals(100, meetTaskBefore.target)

        // 修改为 150 → 任务表 target 必须同步
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 150))
        val meetTaskAfter = db.taskDao().getByDate(today).first { it.metric == "MEET" }
        assertEquals(150, meetTaskAfter.target)

        // 首页读取的今日配置也必须为 150
        assertEquals(150, taskService.getTodayConfig().meetTarget)
    }

    @Test
    fun 配置Flow在修改后应发出新值() = runTest {
        val taskService = AppContainer.dailyTaskService
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 100))

        // 收集响应式配置流
        val emitted = mutableListOf<DailyTaskConfig>()
        val job = launch {
            taskService.watchTodayConfig()
                .take(2)
                .toList(emitted)
        }

        // 第一次发射: 当前值 100
        // 修改后第二次发射: 150
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 150))
        job.join()

        assertEquals(2, emitted.size)
        assertEquals(150, emitted.last().meetTarget)
    }

    @Test
    fun 修改后重读配置仍为最新值() = runTest {
        val taskService = AppContainer.dailyTaskService
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 100))
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 150))
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 200))

        assertEquals(200, taskService.getTodayConfig().meetTarget)
    }

    @Test
    fun watchTodayConfig_初始发射当前值() = runTest {
        val taskService = AppContainer.dailyTaskService
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 100))

        val config = taskService.watchTodayConfig().first()
        assertEquals(100, config.meetTarget)
    }
}
