package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.DefaultTaskConfig
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.DailyTaskConfig
import com.salesquest.sales_quest.ui.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 首页 ViewModel 状态随基础任务配置修改实时同步测试
 *
 * 场景: 100 → 150 → 200, 修改后首页 uiState.config 及任务行 target 同步更新,
 * 无需重新创建 ViewModel / 重启页面。
 *
 * 说明: 使用真实线程轮询等待 (Room IO 协程不受 mainClock 控制, 与 waitForDbValue 同思路)。
 */
@RunWith(RobolectricTestRunner::class)
class HomeViewModelConfigSyncTest {

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

    private fun cfg(meet: Int) = DailyTaskConfig(
        meetTarget = meet,
        queryTarget = DefaultTaskConfig.recommendedQueryTarget,
        dealTarget = DefaultTaskConfig.recommendedDealTarget,
        includeMeet = true,
        includeQuery = true,
        includeDeal = false
    )

    @Test
    fun 修改配置后首页状态自动更新() = runBlocking {
        val taskService = AppContainer.dailyTaskService
        taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 100))

        val vm = HomeViewModel()
        // 订阅以触发 stateIn 上游 collect, 持续保持订阅使 WhileSubscribed 不停止
        val job = launch(Dispatchers.Default) { vm.uiState.collect { } }

        try {
            // 初始: 首页配置及任务行 target 均为 100
            waitForState(vm, predicate = { it.isSynced(100) })
            assertEquals(100, vm.uiState.value.tasks.firstOrNull { it.metric == "MEET" }?.target)

            // 修改为 150: 首页自动更新, 无需重建 ViewModel
            taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 150))
            waitForState(vm, predicate = { it.isSynced(150) })

            // 修改为 200: 再次自动更新
            taskService.setDayConfig(System.currentTimeMillis(), cfg(meet = 200))
            waitForState(vm, predicate = { it.isSynced(200) })
        } finally {
            job.cancel()
        }
    }

    /** 轮询等待 uiState 达到稳定一致状态 (配置与任务行 target 同时到位) */
    private fun waitForState(
        vm: HomeViewModel,
        predicate: (com.salesquest.sales_quest.ui.HomeUiState) -> Boolean,
        timeoutMs: Long = 8000
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            // 推进 Robolectric 主 looper, 让 viewModelScope 的 combine/stateIn 协程执行
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            val state = vm.uiState.value
            if (predicate(state)) return
            Thread.sleep(50)
        }
        assertEquals("uiState 未在 $timeoutMs ms 内达到目标", true, false)
    }

    /** 配置与任务行 target 均同步到目标值, 才算稳定一致状态 */
    private fun com.salesquest.sales_quest.ui.HomeUiState.isSynced(target: Int): Boolean {
        val taskTarget = tasks.firstOrNull { it.metric == "MEET" }?.target
        return config?.meetTarget == target && taskTarget == target
    }
}
