package com.salesquest.sales_quest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.ui.navigation.SalesQuestApp
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 底部导航测试 (v1.0.0 - 4 tab, 设置不在底部)
 *
 * 底部 tab: 作战 / 客户 / 数据 / 成就
 * 设置通过首页右上角图标进入
 */
@RunWith(RobolectricTestRunner::class)
class NavigationTest {

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
    fun 底部导航应显示四个tab且默认在首页() {
        composeRule.setContent { SalesQuestApp() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("作战").assertIsDisplayed()
        composeRule.onNodeWithText("客户").assertIsDisplayed()
        composeRule.onNodeWithText("数据").assertIsDisplayed()
        composeRule.onNodeWithText("成就").assertIsDisplayed()
        // 设置不在底部导航
        composeRule.onNodeWithText("今日战绩").assertIsDisplayed()
    }

    @Test
    fun 切换到客户tab应显示客户列表() {
        composeRule.setContent { SalesQuestApp() }
        composeRule.waitForIdle()

        composeRule.onNode(hasText("客户") and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("还没有客户, 点击右上角添加").assertIsDisplayed()
    }

    @Test
    fun 切换到数据tab应显示数据分析() {
        composeRule.setContent { SalesQuestApp() }
        composeRule.waitForIdle()

        composeRule.onNode(hasText("数据") and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("数据分析").assertIsDisplayed()
    }

    @Test
    fun 数据页应显示总结入口() {
        composeRule.setContent { SalesQuestApp() }
        composeRule.waitForIdle()

        composeRule.onNode(hasText("数据") and hasClickAction()).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("数据分析").assertIsDisplayed()
        composeRule.onNodeWithText("总结").assertExists()
        composeRule.onNodeWithText("今日总结 / 周总结").assertExists()
    }

    @Test
    fun 重复点击同一tab后导航栈不应堆积页面() {
        composeRule.setContent { SalesQuestApp() }
        composeRule.waitForIdle()

        val customersTab = composeRule.onNode(hasText("客户") and hasClickAction())
        repeat(5) {
            customersTab.performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithText("还没有客户, 点击右上角添加").assertIsDisplayed()
    }
}
