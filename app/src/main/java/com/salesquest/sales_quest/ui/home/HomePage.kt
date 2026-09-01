package com.salesquest.sales_quest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.services.DailyTaskConfig
import com.salesquest.sales_quest.services.LevelProgress
import com.salesquest.sales_quest.ui.BattleStats
import com.salesquest.sales_quest.ui.ExecutionRecordUi
import com.salesquest.sales_quest.ui.HomeUiState
import com.salesquest.sales_quest.ui.WeekDayStats
import kotlinx.coroutines.launch

/**
 * 作战首页 - 核心使用闭环: 今日战绩 + 记录数据 + 今日任务
 *
 * 性能优化: 拆分为独立子 Composable, 各区域只读取自己需要的数据,
 * 避免一个数字变化导致整页重组
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    onNavigateToTaskConfig: () -> Unit = {},
    onViewAllExecutionRecords: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editMetric by remember { mutableStateOf<EditMetricRequest?>(null) }
    var showDailyEntry by remember { mutableStateOf(false) }
    var showExecRecordSheet by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 100.dp)
        ) {
            // 等级卡片: 只读取等级相关字段
            LevelSection(
                levelProgress = state.levelProgress,
                totalXp = state.totalXp,
                streakDays = state.streakDays
            )
            Spacer(Modifier.height(12.dp))

            // 今日战绩: 只读取 stats 字段
            BattleStatsSection(
                stats = state.stats,
                onRecordData = { showDailyEntry = true },
                onEditMetric = { req -> editMetric = req }
            )
            Spacer(Modifier.height(16.dp))

            // 今日执行记录: 分段记录
            ExecutionRecordsSection(
                records = state.executionRecords,
                onAddRecord = { showExecRecordSheet = true },
                onViewAll = onViewAllExecutionRecords
            )
            Spacer(Modifier.height(16.dp))

            // 今日任务: 只读取 tasks + config 字段
            TaskSection(
                tasks = state.tasks,
                config = state.config,
                loading = state.loading,
                onNavigateToTaskConfig = onNavigateToTaskConfig
            )
            Spacer(Modifier.height(16.dp))

            // 本周战绩: 只读取 weekStats 字段
            WeeklyBattleCard(weekStats = state.weekStats)
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    editMetric?.let { req ->
        EditMetricDialog(
            request = req,
            onDismiss = { editMetric = null },
            onSave = { v ->
                scope.launch {
                    try {
                        req.onSave(v)
                        editMetric = null
                        snackbarHostState.showSnackbar("已保存")
                    } catch (e: IllegalArgumentException) {
                        snackbarHostState.showSnackbar(e.message ?: "保存失败")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("保存失败: ${e.message}")
                    }
                }
            }
        )
    }

    if (showDailyEntry) {
        ModalBottomSheet(
            onDismissRequest = { showDailyEntry = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            QuickActionSheet(onDone = { showDailyEntry = false })
        }
    }

    if (showExecRecordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExecRecordSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            ExecutionRecordSheet(onDone = { showExecRecordSheet = false })
        }
    }
}

/** 等级区域 — 仅依赖 levelProgress / totalXp / streakDays */
@Composable
private fun LevelSection(
    levelProgress: LevelProgress?,
    totalXp: Int,
    streakDays: Int
) {
    val level = levelProgress?.currentLevel ?: AppLevels.levels.first()
    val nextLevel = levelProgress?.nextLevel
    val xpProgress = if (nextLevel != null) {
        val range = (nextLevel.xpRequired - level.xpRequired).toDouble()
        val done = (totalXp - level.xpRequired).toDouble()
        if (range <= 0) 1.0 else (done / range).coerceIn(0.0, 1.0)
    } else 1.0
    LevelCard(
        level = level.level,
        title = level.title,
        totalXp = totalXp,
        currentLevelXp = level.xpRequired,
        nextLevelXp = nextLevel?.xpRequired ?: level.xpRequired,
        progress = xpProgress,
        streakDays = streakDays
    )
}

/** 今日战绩区域 — 仅依赖 stats, 修改见人数不会触发等级区域重组 */
@Composable
private fun BattleStatsSection(
    stats: BattleStats,
    onRecordData: () -> Unit,
    onEditMetric: (EditMetricRequest) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "今日战绩",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRecordData) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("记录数据", style = MaterialTheme.typography.labelLarge)
        }
    }
    Spacer(Modifier.height(4.dp))
    Row {
        EditableStatCard(
            value = stats.peopleSeen,
            label = "见人",
            icon = Icons.Filled.Groups,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f),
            onTap = {
                onEditMetric(EditMetricRequest("见人数", stats.peopleSeen, "人") { v ->
                    AppContainer.quickActionService.setPeopleSeen(v)
                })
            }
        )
        Spacer(Modifier.width(8.dp))
        EditableStatCard(
            value = stats.queries,
            label = "查询",
            icon = Icons.Filled.Search,
            color = Color(0xFF9C27B0),
            modifier = Modifier.weight(1f),
            onTap = {
                onEditMetric(EditMetricRequest("查询数", stats.queries, "次") { v ->
                    AppContainer.quickActionService.setQuery(v)
                })
            }
        )
        Spacer(Modifier.width(8.dp))
        EditableStatCard(
            value = stats.deals,
            label = "成交",
            icon = Icons.Filled.Celebration,
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f),
            onTap = {
                onEditMetric(EditMetricRequest("成交数", stats.deals, "单") { v ->
                    AppContainer.quickActionService.setDeal(v)
                })
            }
        )
    }
}

/** 今日任务区域 — 仅依赖 tasks / config / loading */
@Composable
private fun TaskSection(
    tasks: List<DailyTaskEntity>,
    config: DailyTaskConfig?,
    loading: Boolean,
    onNavigateToTaskConfig: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "今日任务",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onNavigateToTaskConfig) {
            Icon(Icons.Filled.Settings, contentDescription = "基础任务设置", modifier = Modifier.size(20.dp))
        }
    }
    Spacer(Modifier.height(8.dp))

    if (loading) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (tasks.isEmpty()) {
        EmptyTaskCard(config = config)
    } else {
        tasks.forEachIndexed { index, task ->
            val (label, icon, color) = taskMeta(task.metric)
            TaskRow(
                label = label,
                icon = icon,
                color = color,
                progress = task.progress,
                target = task.target,
                completed = task.completed
            )
            if (index < tasks.size - 1) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

internal data class EditMetricRequest(
    val label: String,
    val currentValue: Int,
    val suffix: String,
    val onSave: suspend (Int) -> Unit
)

@Composable
internal fun EditMetricDialog(
    request: EditMetricRequest,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var text by remember { mutableStateOf(request.currentValue.toString()) }
    var saving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改 ${request.label}") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(request.label) },
                suffix = { Text(request.suffix) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    saving = true
                    val parsed = text.trim().toIntOrNull() ?: 0
                    if (parsed >= 0) onSave(parsed)
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

internal fun taskMeta(metric: String): Triple<String, ImageVector, Color> = when (metric) {
    "MEET" -> Triple("见人", Icons.Filled.Groups, Color(0xFF2196F3))
    "QUERY" -> Triple("查询", Icons.Filled.Search, Color(0xFF9C27B0))
    "DEAL" -> Triple("成交", Icons.Filled.Celebration, Color(0xFFF44336))
    else -> Triple(metric, Icons.Filled.Check, Color.Gray)
}

/** 等级卡片: 等级徽章 + 等级名 + XP 进度条 + 连续作战天数 */
@Composable
fun LevelCard(
    level: Int,
    title: String,
    totalXp: Int,
    currentLevelXp: Int,
    nextLevelXp: Int,
    progress: Double,
    streakDays: Int
) {
    val theme = MaterialTheme.colorScheme
    val xpInLevel = totalXp - currentLevelXp
    val rawSpan = nextLevelXp - currentLevelXp
    val isMax = rawSpan <= 0
    val span = if (isMax) 1 else rawSpan
    val barValue = if (isMax) 1f else progress.coerceIn(0.0, 1.0).toFloat()
    val xpText = if (isMax) "已满级" else "$xpInLevel/$span XP"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        theme.primary.copy(alpha = 0.10f),
                        theme.tertiary.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(theme.primary, theme.tertiary))),
            contentAlignment = Alignment.Center
        ) {
            Text("L$level", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (streakDays > 0) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🔥 $streakDays", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(xpText, style = MaterialTheme.typography.labelSmall, color = theme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { barValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = theme.primary,
                trackColor = theme.surfaceContainerHighest
            )
        }
    }
}

/** 可编辑的大数字统计卡片 — 点击直接修改 */
@Composable
fun EditableStatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Column(
        modifier = modifier
            .height(116.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onTap)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            "$value",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(10.dp))
        }
    }
}

/** 今日任务行 */
@Composable
fun TaskRow(
    label: String,
    icon: ImageVector,
    color: Color,
    progress: Int,
    target: Int,
    completed: Boolean
) {
    val theme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (completed) Color(0xFF4CAF50).copy(alpha = 0.08f) else theme.surfaceContainerLow)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (completed) Icons.Filled.CheckCircle else icon,
            contentDescription = null,
            tint = if (completed) Color(0xFF4CAF50) else color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Text(
            "$progress/$target",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (completed) Color(0xFF4CAF50) else theme.onSurface
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            if (completed) Icons.Filled.Check else Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = if (completed) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** 空任务卡片 */
@Composable
fun EmptyTaskCard(config: DailyTaskConfig?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            if (config == null) "点击右侧设置按钮配置今日基础任务" else "今日未设置任何基础任务\n请在设置中添加指标",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 今日执行记录区域 — 轻量展示最近几条分段记录 */
@Composable
private fun ExecutionRecordsSection(
    records: List<ExecutionRecordUi>,
    onAddRecord: () -> Unit,
    onViewAll: () -> Unit
) {
    // 标题行
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "今日执行记录",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (records.isNotEmpty()) {
            TextButton(onClick = onViewAll) {
                Text("查看全部", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.width(4.dp))
        }
        TextButton(onClick = onAddRecord) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("记录", style = MaterialTheme.typography.labelLarge)
        }
    }
    Spacer(Modifier.height(4.dp))

    if (records.isEmpty()) {
        // 空状态: 轻量提示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(onClick = onAddRecord)
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "还没有执行记录, 点击添加今日第一条",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
    } else {
        // 显示最近 4 条 (倒序, 最新在上)
        val display = records.takeLast(4).reversed()
        display.forEachIndexed { index, record ->
            ExecutionRecordCompactRow(record = record)
            if (index < display.size - 1) {
                Spacer(Modifier.height(2.dp))
            }
        }
        if (records.size > 4) {
            Spacer(Modifier.height(2.dp))
            TextButton(onClick = onViewAll, modifier = Modifier.fillMaxWidth()) {
                Text("查看全部 ${records.size} 条", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** 紧凑执行记录行 — 首页轻量展示 */
@Composable
private fun ExecutionRecordCompactRow(record: ExecutionRecordUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 时间标签
        Text(
            record.timeLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(56.dp)
        )
        // 三个增量数据
        CompactDelta("+${record.peopleSeen}", "见", Color(0xFF2196F3), Modifier.weight(1f))
        CompactDelta("+${record.queries}", "查", Color(0xFF9C27B0), Modifier.weight(1f))
        CompactDelta("+${record.deals}", "成", Color(0xFFF44336), Modifier.weight(1f))
    }
}

@Composable
private fun CompactDelta(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.width(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
