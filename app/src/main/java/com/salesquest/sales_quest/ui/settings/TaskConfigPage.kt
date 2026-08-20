package com.salesquest.sales_quest.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.DefaultTaskConfig
import kotlinx.coroutines.launch

/** 基础任务设置页 - 自定义目标 + 是否纳入成交 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskConfigPage(
    onBack: () -> Unit = {},
    viewModel: TaskConfigViewModel = viewModel()
) {
    val config by viewModel.config.collectAsState()
    val locked by viewModel.locked.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var meetTarget by remember { mutableStateOf(DefaultTaskConfig.recommendedMeetTarget) }
    var queryTarget by remember { mutableStateOf(DefaultTaskConfig.recommendedQueryTarget) }
    var dealTarget by remember { mutableStateOf(DefaultTaskConfig.recommendedDealTarget) }
    var includeMeet by remember { mutableStateOf(DefaultTaskConfig.recommendedIncludeMeet) }
    var includeQuery by remember { mutableStateOf(DefaultTaskConfig.recommendedIncludeQuery) }
    var includeDeal by remember { mutableStateOf(DefaultTaskConfig.recommendedIncludeDeal) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(config) {
        if (config != null) {
            meetTarget = config!!.meetTarget
            queryTarget = config!!.queryTarget
            dealTarget = config!!.dealTarget
            includeMeet = config!!.includeMeet
            includeQuery = config!!.includeQuery
            includeDeal = config!!.includeDeal
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("基础任务设置") },
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
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // === 锁定提示 ===
                InfoBanner(
                    text = if (locked) "今日任务已锁定 (已产生数据), 不可修改"
                    else "设置每日最低目标, 完成全部目标即可达成今日作战",
                    locked = locked
                )
                Spacer(Modifier.height(12.dp))

                // === 推荐目标按钮 ===
                if (!locked) {
                    OutlinedButton(
                        onClick = {
                            meetTarget = DefaultTaskConfig.recommendedMeetTarget
                            queryTarget = DefaultTaskConfig.recommendedQueryTarget
                            dealTarget = DefaultTaskConfig.recommendedDealTarget
                            includeMeet = DefaultTaskConfig.recommendedIncludeMeet
                            includeQuery = DefaultTaskConfig.recommendedIncludeQuery
                            includeDeal = DefaultTaskConfig.recommendedIncludeDeal
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Recommend, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("使用推荐目标 (见人100 / 查询5 / 成交不参与)")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // === 每日最低目标 ===
                Text(
                    "每日最低目标",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                MetricConfigCard(
                    label = "见人数",
                    icon = Icons.Filled.Groups,
                    color = Color(0xFF2196F3),
                    included = includeMeet,
                    target = meetTarget,
                    locked = locked,
                    unit = "人",
                    onTargetChanged = { meetTarget = it },
                    onToggleInclude = { includeMeet = true },
                    onRemove = { includeMeet = false }
                )
                Spacer(Modifier.height(8.dp))

                MetricConfigCard(
                    label = "查询数",
                    icon = Icons.Filled.Search,
                    color = Color(0xFF9C27B0),
                    included = includeQuery,
                    target = queryTarget,
                    locked = locked,
                    unit = "次",
                    onTargetChanged = { queryTarget = it },
                    onToggleInclude = { includeQuery = true },
                    onRemove = { includeQuery = false }
                )
                Spacer(Modifier.height(8.dp))

                MetricConfigCard(
                    label = "成交数",
                    icon = Icons.Filled.Celebration,
                    color = Color(0xFFF44336),
                    included = includeDeal,
                    target = dealTarget,
                    locked = locked,
                    unit = "单",
                    isDeal = true,
                    onTargetChanged = { dealTarget = it },
                    onToggleInclude = { includeDeal = true },
                    onRemove = { includeDeal = false }
                )

                Spacer(Modifier.height(16.dp))

                // === 完成说明 ===
                CompletionHint()
                Spacer(Modifier.height(24.dp))

                // === 保存按钮 ===
                if (!locked) {
                    Button(
                        onClick = {
                            if (saving) return@Button
                            if (!includeMeet && !includeQuery && !includeDeal) {
                                scope.launch { snackbarHostState.showSnackbar("请至少选择一个指标") }
                                return@Button
                            }
                            saving = true
                            scope.launch {
                                try {
                                    viewModel.save(
                                        meetTarget = meetTarget,
                                        queryTarget = queryTarget,
                                        dealTarget = dealTarget,
                                        includeMeet = includeMeet,
                                        includeQuery = includeQuery,
                                        includeDeal = includeDeal
                                    )
                                    snackbarHostState.showSnackbar("已保存")
                                    onBack()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("保存失败: ${e.message}")
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text("保存")
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InfoBanner(text: String, locked: Boolean) {
    val bg = if (locked) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    val fg = if (locked) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = fg)
    }
}

@Composable
fun MetricConfigCard(
    label: String,
    icon: ImageVector,
    color: Color,
    included: Boolean,
    target: Int,
    locked: Boolean,
    unit: String,
    isDeal: Boolean = false,
    onTargetChanged: (Int) -> Unit,
    onToggleInclude: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (isDeal) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("默认不参与", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (included) "参与" else "不参与",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (included) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            if (included) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("目标", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(12.dp))
                    EditableTargetBox(
                        target = target,
                        color = color,
                        locked = locked,
                        onTargetChanged = onTargetChanged
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!locked) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = onRemove,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("移出基础任务", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF44336))
                    }
                }
            } else if (!locked) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onToggleInclude,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("加入基础任务", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/**
 * 可编辑的目标值控件 — 视觉上保留原数字显示 Box 风格,
 * 点击后可直接输入数字, 不使用标准表单输入框样式.
 */
@Composable
private fun EditableTargetBox(
    target: Int,
    color: Color,
    locked: Boolean,
    onTargetChanged: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var text by remember { mutableStateOf(target.toString()) }

    // 外部 target 变化时(配置加载 / 推荐目标), 同步显示文本
    LaunchedEffect(target) {
        if (!isFocused) {
            text = target.toString()
        }
    }

    // 失焦时校验并规范化输入
    LaunchedEffect(isFocused) {
        if (!isFocused) {
            val num = text.toIntOrNull()
            val validNum = num?.coerceIn(0, 9999) ?: 0
            text = validNum.toString()
            onTargetChanged(validNum)
        }
    }

    Box(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .then(
                if (isFocused && !locked) {
                    Modifier.border(1.dp, color, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = text,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }
                if (filtered.length <= 4) {
                    text = filtered
                    val num = filtered.toIntOrNull()
                    if (num != null && num in 0..9999) {
                        onTargetChanged(num)
                    }
                }
            },
            enabled = !locked,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(color),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun CompletionHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF4CAF50).copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "完成以上全部目标即可完成今日作战\n连续完成每日基础任务 → 连续作战 +1",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2E7D32)
        )
    }
}
