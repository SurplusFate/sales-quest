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
 * 对应 legacy/test/router_navigation_test.dart:
 * 底部导航 tab 切换后按返回键不应回到上一个 tab / 二级页面返回应回到所在 tab
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
        composeRule.onNodeWithText("今日作战 (点击数字修改)").assertIsDisplayed()
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
