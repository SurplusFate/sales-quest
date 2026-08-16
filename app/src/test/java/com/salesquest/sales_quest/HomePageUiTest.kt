package com.salesquest.sales_quest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.ui.home.HomePage
import com.salesquest.sales_quest.ui.home.QuickActionSheet
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 对应 legacy/test/home_page_test.dart 与 legacy/test/router_navigation_test.dart 的核心场景
 */
@RunWith(RobolectricTestRunner::class)
class HomePageUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun 首页应显示三个统计卡片和今日作战标题() {
        composeRule.setContent { HomePage() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("今日作战 (点击数字修改)").assertIsDisplayed()
        composeRule.onNodeWithText("见人").assertIsDisplayed()
        composeRule.onNodeWithText("查询").assertIsDisplayed()
        composeRule.onNodeWithText("成交").assertIsDisplayed()
    }

    @Test
    fun 点击统计卡片应弹出编辑对话框() {
        composeRule.setContent { HomePage() }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("0")[0].performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("修改 见人数").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsDisplayed()
        composeRule.onNodeWithText("取消").assertIsDisplayed()
    }

    @Test
    fun 在对话框中输入新值并保存应更新数据() = runBlocking {
        composeRule.setContent { HomePage() }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("0")[0].performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("修改 见人数").assertIsDisplayed()
        val dialogInput = composeRule.onAllNodes(hasSetTextAction())[0]
        dialogInput.performTextClearance()
        dialogInput.performTextInput("50")
        composeRule.onNodeWithText("保存").performClick()
        waitForDbValue("total_meets", 50)
    }

    @Test
    fun 取消编辑不应修改数据() = runBlocking {
        composeRule.setContent { HomePage() }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("0")[0].performClick()
        composeRule.waitForIdle()

        val cancelInput = composeRule.onAllNodes(hasSetTextAction())[0]
        cancelInput.performTextClearance()
        cancelInput.performTextInput("100")
        composeRule.onNodeWithText("取消").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("修改 见人数").assertDoesNotExist()
        assertEquals(0, db.settingDao().getInt("total_meets"))
    }

    @Test
    fun 快速记录面板应能批量修改三个数据() = runBlocking {
        composeRule.setContent { QuickActionSheet(onDone = {}) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("快速记录").assertIsDisplayed()
        composeRule.onNodeWithText("见人数").assertIsDisplayed()
        composeRule.onNodeWithText("查询数").assertIsDisplayed()
        composeRule.onNodeWithText("成交数").assertIsDisplayed()

        val inputs = composeRule.onAllNodes(hasSetTextAction())
        inputs[0].performTextClearance()
        inputs[0].performTextInput("80")
        composeRule.waitForIdle()
        inputs[1].performTextClearance()
        inputs[1].performTextInput("20")
        composeRule.waitForIdle()
        inputs[2].performTextClearance()
        inputs[2].performTextInput("5")

        composeRule.onNodeWithText("保存").performClick()
        val todayKey = DateUtil.dateKey()
        waitForDbValue("people_seen_$todayKey", 80)
        waitForDbValue("queries_$todayKey", 20)
        waitForDbValue("deals_$todayKey", 5)
    }

    /** 轮询等待 Room 写入生效 (IO 协程不在 mainClock 控制内) */
    private fun waitForDbValue(key: String, expected: Int, timeoutMs: Long = 5000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var last = -1
        while (System.currentTimeMillis() < deadline) {
            composeRule.mainClock.advanceTimeBy(50)
            composeRule.waitForIdle()
            last = runBlocking { db.settingDao().getInt(key) }
            if (last == expected) return
        }
        assertEquals(expected, last)
    }
}
