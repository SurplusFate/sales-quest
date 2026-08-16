package com.salesquest.sales_quest.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.ui.graphics.vector.ImageVector

/** 底部导航栏 tab */
enum class AppTab(
    val label: String,
    val icon: ImageVector,
    val activeIcon: ImageVector,
    val route: String
) {
    HOME("作战", Icons.Outlined.Home, Icons.Filled.Home, "home"),
    CUSTOMERS("客户", Icons.Outlined.People, Icons.Filled.People, "customers"),
    DATA("数据", Icons.Outlined.BarChart, Icons.Filled.BarChart, "data"),
    ACHIEVEMENTS("成就", Icons.Outlined.EmojiEvents, Icons.Filled.EmojiEvents, "achievements")
}
