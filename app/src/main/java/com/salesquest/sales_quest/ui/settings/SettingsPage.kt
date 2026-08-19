package com.salesquest.sales_quest.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.data.AppDatabase
import kotlinx.coroutines.launch

/** 设置页 - 数据 / 云备份 / 应用 三个区域 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    onBack: () -> Unit = {},
    onOpenTaskConfig: () -> Unit = {},
    onOpenConfigFile: () -> Unit = {},
    onOpenWebDav: () -> Unit = {},
    onOpenSummary: () -> Unit = {},
    onOpenLogs: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearTodayDialog by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { SettingsGroup("基础任务") }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Flag,
                    title = "基础任务设置",
                    subtitle = "自定义每日见人 / 查询 / 成交目标",
                    onClick = onOpenTaskConfig
                )
            }

            item { SettingsGroup("数据") }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Description,
                    title = "配置文件",
                    subtitle = "导入 / 导出 JSON 配置",
                    onClick = onOpenConfigFile
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.EditNote,
                    title = "总结",
                    subtitle = "每日总结 / 周总结",
                    onClick = onOpenSummary
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Today,
                    title = "清除今日数据",
                    subtitle = "清除今天的见人 / 查询 / 成交数据",
                    onClick = { showClearTodayDialog = true }
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.DeleteSweep,
                    title = "清除所有数据",
                    subtitle = "删除全部客户、记录、XP 和成就",
                    iconTint = Color(0xFFF44336),
                    onClick = { showClearAllDialog = true }
                )
            }

            item { SettingsGroup("云备份") }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Cloud,
                    title = "坚果云 WebDAV",
                    subtitle = "账号配置 / 备份 / 恢复 / 自动备份",
                    onClick = onOpenWebDav
                )
            }

            item { SettingsGroup("应用") }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Info,
                    title = "当前版本",
                    trailing = { Text("V1.0") }
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Storage,
                    title = "数据库版本",
                    trailing = { Text("v${AppDatabase.VERSION}") }
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.SportsEsports,
                    title = "关于 Sales Quest",
                    subtitle = "游戏化销售作战系统"
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.Backup,
                    title = "开发日志",
                    onClick = onOpenLogs
                )
            }
            item {
                SettingsListItem(
                    icon = Icons.Filled.MilitaryTech,
                    title = "等级系统",
                    subtitle = "共 ${AppLevels.levels.size} 个等级, 含晋级条件"
                )
            }
        }
    }

    if (showClearTodayDialog) {
        AlertDialog(
            onDismissRequest = { showClearTodayDialog = false },
            title = { Text("清除今日数据") },
            text = { Text("将清除今天的见人 / 查询 / 成交数据及今日任务进度。\n累计数据不受影响。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearTodayDialog = false
                        scope.launch {
                            try {
                                viewModel.clearToday()
                                snackbarHostState.showSnackbar("今日数据已清除")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("清除失败: ${e.message}")
                            }
                        }
                    }
                ) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearTodayDialog = false }) { Text("取消") }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("清除所有数据") },
            text = { Text("这将永久删除所有客户、记录、XP 和成就, 且不可撤销!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        scope.launch {
                            try {
                                viewModel.clearAll()
                                snackbarHostState.showSnackbar("所有数据已清除")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("清除失败: ${e.message}")
                            }
                        }
                    }
                ) { Text("全部清除", color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun SettingsGroup(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null, tint = iconTint) },
        trailingContent = trailing ?: {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        },
        modifier = if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        } else {
            Modifier.fillMaxWidth()
        }
    )
}
