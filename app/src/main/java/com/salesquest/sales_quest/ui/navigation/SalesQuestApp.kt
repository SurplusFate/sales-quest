package com.salesquest.sales_quest.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.salesquest.sales_quest.ui.home.QuickActionSheet
import com.salesquest.sales_quest.ui.settings.SettingsPage
import com.salesquest.sales_quest.ui.settings.TaskConfigPage
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier as UiModifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesQuestApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val currentTab = AppTab.entries.firstOrNull { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    var showQuickAction by remember { mutableStateOf(false) }

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
                        label = { androidx.compose.material3.Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab != null) {
                FloatingActionButton(
                    onClick = { showQuickAction = true },
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = "快速记录")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = UiModifier.padding(innerPadding)
        ) {
            // 底部 4 tab
            composable("home") {
                HomePage(onNavigateToTaskConfig = { navController.navigate("settings/task-config") })
            }
            composable("customers") {
                CustomerListPage(
                    onAddCustomer = { navController.navigate("customer/new") },
                    onOpenCustomer = { id -> navController.navigate("customer/$id") }
                )
            }
            composable("data") { AnalyticsPage() }
            composable("achievements") {
                AchievementPage(onOpenXpLevel = { navController.navigate("xp") })
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
                    onOpenLogs = { navController.navigate("dev/logs") }
                )
            }
            composable("settings/task-config") {
                TaskConfigPage(onBack = { navController.popBackStack() })
            }
            composable("dev/logs") {
                LogViewerPage(onBack = { navController.popBackStack() })
            }
        }
    }

    if (showQuickAction) {
        ModalBottomSheet(
            onDismissRequest = { showQuickAction = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            QuickActionSheet(onDone = { showQuickAction = false })
        }
    }
}
