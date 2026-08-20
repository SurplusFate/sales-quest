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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Settings
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
import com.salesquest.sales_quest.services.DailyTaskConfig
import com.salesquest.sales_quest.ui.HomeUiState
import kotlinx.coroutines.launch

/** 作战首页 - 核心使用闭环: 今日战绩 + 记录数据 + 今日任务 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    onNavigateToTaskConfig: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var editMetric by remember { mutableStateOf<EditMetricRequest?>(null) }
    var showDailyEntry by remember { mutableStateOf(false) }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 100.dp)
        ) {
            // === 等级卡片 (使用 LevelService 判定) ===
            val progress = state.levelProgress
            val level = progress?.currentLevel ?: AppLevels.levels.first()
            val nextLevel = progress?.nextLevel
            val xpProgress = if (nextLevel != null) {
                val range = (nextLevel.xpRequired - level.xpRequired).toDouble()
                val done = (state.totalXp - level.xpRequired).toDouble()
                if (range <= 0) 1.0 else (done / range).coerceIn(0.0, 1.0)
            } else 1.0
            LevelCard(
                level = level.level,
                title = level.title,
                totalXp = state.totalXp,
                currentLevelXp = level.xpRequired,
                nextLevelXp = nextLevel?.xpRequired ?: level.xpRequired,
                progress = xpProgress,
                streakDays = state.streakDays
            )
            Spacer(Modifier.height(12.dp))

            // === 今日战绩 (仅展示, 点击编辑) ===
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "今日战绩",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showDailyEntry = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("记录数据", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row {
                EditableStatCard(
                    value = state.stats.peopleSeen,
                    label = "见人",
                    icon = Icons.Filled.Groups,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f),
                    onTap = {
                        editMetric = EditMetricRequest("见人数", state.stats.peopleSeen, "人") { v ->
                            AppContainer.quickActionService.setPeopleSeen(v)
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                EditableStatCard(
                    value = state.stats.queries,
                    label = "查询",
                    icon = Icons.Filled.Search,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f),
                    onTap = {
                        editMetric = EditMetricRequest("查询数", state.stats.queries, "次") { v ->
                            AppContainer.quickActionService.setQuery(v)
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                EditableStatCard(
                    value = state.stats.deals,
                    label = "成交",
                    icon = Icons.Filled.Celebration,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f),
                    onTap = {
                        editMetric = EditMetricRequest("成交数", state.stats.deals, "单") { v ->
                            AppContainer.quickActionService.setDeal(v)
                        }
                    }
                )
            }
            Spacer(Modifier.height(16.dp))

            // === 今日任务 (仅展示目标进度) ===
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

            if (state.loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.tasks.isEmpty()) {
                EmptyTaskCard(config = state.config)
            } else {
                state.tasks.forEachIndexed { index, task ->
                    val (label, icon, color) = taskMeta(task.metric)
                    TaskRow(
                        label = label,
                        icon = icon,
                        color = color,
                        progress = task.progress,
                        target = task.target,
                        completed = task.completed
                    )
                    if (index < state.tasks.size - 1) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // === 本周战绩 ===
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

@Composable
internal fun HomeUiState.asSnapshot(): HomeUiState = this
