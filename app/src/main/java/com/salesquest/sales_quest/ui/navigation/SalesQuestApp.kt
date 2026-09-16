package com.salesquest.sales_quest.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.salesquest.sales_quest.ui.theme.GlassDock
import com.salesquest.sales_quest.ui.theme.GlassRoot
import com.salesquest.sales_quest.ui.theme.LocalIsDark
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.salesquest.sales_quest.ui.AppTab
import com.salesquest.sales_quest.ui.achievements.AchievementPage
import com.salesquest.sales_quest.ui.achievements.XpLevelPage
import com.salesquest.sales_quest.ui.customers.CustomerDetailPage
import com.salesquest.sales_quest.ui.customers.CustomerFormPage
import com.salesquest.sales_quest.ui.customers.CustomerListPage
import com.salesquest.sales_quest.ui.data.AnalyticsPage
import com.salesquest.sales_quest.ui.dev.LogViewerPage
import com.salesquest.sales_quest.ui.home.HomePage
import com.salesquest.sales_quest.ui.home.ExecutionRecordListPage
import com.salesquest.sales_quest.ui.settings.ConfigPage
import com.salesquest.sales_quest.ui.settings.SettingsPage
import com.salesquest.sales_quest.ui.settings.TaskConfigPage
import com.salesquest.sales_quest.ui.settings.ThemeSettingsPage
import com.salesquest.sales_quest.ui.settings.WebDavPage
import com.salesquest.sales_quest.ui.summary.SummaryPage

@Composable
fun SalesQuestApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val currentTab = AppTab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    // 深浅色切换时强制重建整棵 UI 树:
    // 液态玻璃(backdrop)的绘制快照在部分设备上不会随主题重组自动刷新,
    // key 兜底保证切主题一定以新主题重建全部页面(代价: 导航回到首页, 属预期行为)。
    val isDark = LocalIsDark.current
    key(isDark) {
        GlassRoot(
            bottomBar = { contentBackdrop ->
                // 仅 4 个 tab 路由显示悬浮 DOCK, 全屏路由(设置/详情/总结等)不显示,
                // 避免悬浮胶囊遮挡全屏页底部内容(对齐 GenshinGachaHelper showBottomBar 判定)。
                if (currentTab != null) {
                    GlassDock(
                        contentBackdrop = contentBackdrop,
                        tabs = AppTab.entries,
                        selectedTab = currentTab,
                        onTabSelected = { tab ->
                            if (currentTab != tab) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { _ ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
            ) {
                // 底部 4 tab
                composable("home") {
                    HomePage(
                        onNavigateToTaskConfig = { navController.navigate("settings/task-config") },
                        onViewAllExecutionRecords = { navController.navigate("execution-records") }
                    )
                }
                composable("customers") {
                    CustomerListPage(
                        onAddCustomer = { navController.navigate("customer/new") },
                        onOpenCustomer = { id -> navController.navigate("customer/$id") }
                    )
                }
                composable("data") {
                    AnalyticsPage(
                        onOpenSummary = { navController.navigate("summary") },
                        onOpenExecutionRecords = { navController.navigate("execution-records") }
                    )
                }
                composable("achievements") {
                    AchievementPage(
                        onOpenXpLevel = { navController.navigate("xp") },
                        onOpenSettings = { navController.navigate("settings") }
                    )
                }

                // 全屏路由
                composable("customer/new") {
                    CustomerFormPage(
                        customerId = null,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("customer/{id}") { entry ->
                    CustomerDetailPage(
                        customerId = entry.arguments?.getString("id") ?: "",
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate("customer/$id/edit") }
                    )
                }
                composable("customer/{id}/edit") { entry ->
                    CustomerFormPage(
                        customerId = entry.arguments?.getString("id"),
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("xp") {
                    XpLevelPage(onBack = { navController.popBackStack() })
                }
                composable("settings") {
                    SettingsPage(
                        onBack = { navController.popBackStack() },
                        onOpenTaskConfig = { navController.navigate("settings/task-config") },
                        onOpenConfigFile = { navController.navigate("settings/config") },
                        onOpenWebDav = { navController.navigate("settings/webdav") },
                        onOpenLogs = { navController.navigate("dev/logs") },
                        onOpenThemeSettings = { navController.navigate("settings/theme") }
                    )
                }
                composable("settings/task-config") {
                    TaskConfigPage(onBack = { navController.popBackStack() })
                }
                composable("settings/config") {
                    ConfigPage(onBack = { navController.popBackStack() })
                }
                composable("settings/webdav") {
                    WebDavPage(onBack = { navController.popBackStack() })
                }
                composable("settings/theme") {
                    ThemeSettingsPage(onBack = { navController.popBackStack() })
                }
                composable("summary") {
                    SummaryPage(onBack = { navController.popBackStack() })
                }
                composable("execution-records") {
                    ExecutionRecordListPage(onBack = { navController.popBackStack() })
                }
                composable("dev/logs") {
                    LogViewerPage(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
