package com.salesquest.sales_quest.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = {
                            if (currentTab != tab) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == tab) tab.activeIcon else tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 底部 4 tab
            composable("home") {
                HomePage(
                    onNavigateToTaskConfig = { navController.navigate("settings/task-config") }
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
                    onOpenSummary = { navController.navigate("summary") }
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
            composable("dev/logs") {
                LogViewerPage(onBack = { navController.popBackStack() })
            }
        }
    }
}
