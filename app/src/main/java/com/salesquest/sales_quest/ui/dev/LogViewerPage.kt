package com.salesquest.sales_quest.ui.dev

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.core.LogEntry
import com.salesquest.sales_quest.core.LogLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 运行日志页 - 查看 / 搜索 / 过滤 / 复制 / 清空 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerPage(
    onBack: () -> Unit = {},
    viewModel: LogViewerViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var searchKeyword by remember { mutableStateOf("") }
    var filterLevel by remember { mutableStateOf<LogLevel?>(null) }
    var autoScroll by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val version by viewModel.version.collectAsState()
    val filtered = remember(searchKeyword, filterLevel, version) {
        viewModel.filtered(searchKeyword, filterLevel)
    }

    LaunchedEffect(viewModel.version, autoScroll, filtered.size) {
        if (autoScroll && filtered.isNotEmpty()) {
            listState.scrollToItem(filtered.size - 1)
        }
    }

    fun copyLogs(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("log", text))
        scope.launch { snackbarHostState.showSnackbar(label) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("运行日志") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { autoScroll = !autoScroll }) {
                        Icon(
                            if (autoScroll) Icons.Filled.VerticalAlignBottom else Icons.Filled.VerticalAlignTop,
                            contentDescription = if (autoScroll) "自动滚动到底部" else "关闭自动滚动"
                        )
                    }
                    LogMenu(
                        onExport = { showExportDialog = true },
                        onCopyAll = { copyLogs(AppLogger.exportPlainText(), "日志已复制到剪贴板") },
                        onClear = { showClearDialog = true }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // === 过滤栏 ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    placeholder = { Text("搜索日志...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                LogLevelFilter(selected = filterLevel, onSelect = { filterLevel = it })
            }

            // === 统计栏 ===
            val entries = AppLogger.entries
            val errors = entries.count { it.level == LogLevel.ERROR || it.level == LogLevel.FATAL }
            val warnings = entries.count { it.level == LogLevel.WARNING }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("共 ${entries.size} 条", style = MaterialTheme.typography.labelSmall)
                if (warnings > 0) {
                    Spacer(Modifier.width(12.dp))
                    Text("警告 $warnings", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9800))
                }
                if (errors > 0) {
                    Spacer(Modifier.width(12.dp))
                    Text("错误 $errors", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF44336))
                }
            }

            // === 日志列表 ===
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无日志", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(filtered, key = { it.sequence }) { entry ->
                        LogCard(entry = entry)
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportLogDialog(
            text = AppLogger.exportPlainText(),
            onDismiss = { showExportDialog = false },
            onCopy = {
                copyLogs(AppLogger.exportPlainText(), "日志已复制到剪贴板")
                showExportDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空日志") },
            text = { Text("确定要清空所有日志吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        AppLogger.clear()
                        showClearDialog = false
                    }
                ) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
fun LogMenu(
    onExport: () -> Unit,
    onCopyAll: () -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("导出到文件") },
                leadingIcon = { Icon(Icons.Filled.FileDownload, contentDescription = null) },
                onClick = {
                    expanded = false
                    onExport()
                }
            )
            DropdownMenuItem(
                text = { Text("复制全部") },
                leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                onClick = {
                    expanded = false
                    onCopyAll()
                }
            )
            DropdownMenuItem(
                text = { Text("清空日志") },
                leadingIcon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null) },
                onClick = {
                    expanded = false
                    onClear()
                }
            )
        }
    }
}

@Composable
fun LogLevelFilter(selected: LogLevel?, onSelect: (LogLevel?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(selected?.label ?: "全部", style = MaterialTheme.typography.labelMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("全部") },
                onClick = {
                    expanded = false
                    onSelect(null)
                }
            )
            LogLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level.label) },
                    onClick = {
                        expanded = false
                        onSelect(level)
                    }
                )
            }
        }
    }
}

/** 单条日志卡片 */
@Composable
fun LogCard(entry: LogEntry) {
    val color = levelColor(entry.level)
    val time = formatTime(entry.timestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = if (entry.level == LogLevel.ERROR || entry.level == LogLevel.FATAL) {
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.05f)
            )
        } else {
            androidx.compose.material3.CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    time,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575),
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        entry.level.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    entry.tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                entry.message,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.stackTrace != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    entry.stackTrace,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF616161),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ExportLogDialog(text: String, onDismiss: () -> Unit, onCopy: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出日志") },
        text = {
            Column {
                Text("使用「复制全部」获取完整日志。", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp)
                ) {
                    Text(
                        text,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 12,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onCopy) {
                Text("复制全部")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

private fun levelColor(level: LogLevel): Color = when (level) {
    LogLevel.DEBUG -> Color.Gray
    LogLevel.INFO -> Color(0xFF2196F3)
    LogLevel.WARNING -> Color(0xFFFF9800)
    LogLevel.ERROR -> Color(0xFFF44336)
    LogLevel.FATAL -> Color(0xFFB71C1C)
}

private fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(ts))
}
