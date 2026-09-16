package com.salesquest.sales_quest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.ExecutionRecordService
import kotlinx.coroutines.launch
import com.salesquest.sales_quest.ui.theme.DialogScrimAdjuster

/**
 * 每日基础任务数据录入面板 - 填写"当前最新累计值" (需求1 改造)
 *
 * 语义:
 * - 输入框填的是当天最新累计值, 不是新增量
 * - 保存时自动计算 差值 = 最新累计 - 当前累计, 差值落一条执行记录
 * - 差值为负 (下调) 时先弹确认框, 确认后才写入
 * - 差值全为 0 时不写库
 * - 今天的数据变化由 ExecutionRecordService 统一触发任务/XP/成就刷新
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSheet(
    onDone: () -> Unit,
    initial: Triple<Int, Int, Int> = Triple(0, 0, 0)
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDateKey by remember { mutableStateOf(DateUtil.dateKey()) }
    var meetText by remember { mutableStateOf(initial.first.toString()) }
    var queryText by remember { mutableStateOf(initial.second.toString()) }
    var dealText by remember { mutableStateOf(initial.third.toString()) }
    var saving by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var base by remember { mutableStateOf<ExecutionRecordService.DailyCumulative?>(null) }
    var reloadTick by remember { mutableStateOf(0) }
    var pendingNegativeDelta by remember { mutableStateOf<ExecutionRecordService.CumulativeDelta?>(null) }

    // 切换日期时加载该日期"当前累计值", 并回填为输入初值
    LaunchedEffect(selectedDateKey, reloadTick) {
        val loaded = AppContainer.executionRecordService.getDailyCumulative(selectedDateKey)
        base = loaded
        meetText = loaded.peopleSeen.toString()
        queryText = loaded.queries.toString()
        dealText = loaded.deals.toString()
    }

    val parsedMeet = meetText.trim().toIntOrNull()
    val parsedQuery = queryText.trim().toIntOrNull()
    val parsedDeal = dealText.trim().toIntOrNull()
    val currentBase = base
    val deltaPreview = if (currentBase != null && parsedMeet != null && parsedQuery != null && parsedDeal != null) {
        ExecutionRecordService.CumulativeDelta(
            peopleSeen = parsedMeet - currentBase.peopleSeen,
            queries = parsedQuery - currentBase.queries,
            deals = parsedDeal - currentBase.deals
        )
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "每日基础任务",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (selectedDateKey != DateUtil.dateKey()) {
                Text(
                    "补录",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE65100),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "填写当前最新累计值, 保存后自动计算差值并记入执行记录",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // === 日期选择 ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = { showDatePicker = true })
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("日期：", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(selectedDateKey, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "选择日期", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        QuickInputField(meetText, { meetText = it }, "见人数 (最新累计)", Icons.Filled.Groups, Color(0xFF2196F3), "人")
        Spacer(Modifier.height(12.dp))
        QuickInputField(queryText, { queryText = it }, "查询数 (最新累计)", Icons.Filled.Search, Color(0xFF9C27B0), "次")
        Spacer(Modifier.height(12.dp))
        QuickInputField(dealText, { dealText = it }, "成交数 (最新累计)", Icons.Filled.Celebration, Color(0xFFF44336), "单")

        // === 当前累计 + 本次差值预览 ===
        val baseInfo = currentBase
        if (baseInfo != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                "当前累计  见人 ${baseInfo.peopleSeen} · 查询 ${baseInfo.queries} · 成交 ${baseInfo.deals}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val deltaInfo = deltaPreview
        if (deltaInfo != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                if (deltaInfo.isZero) {
                    "本次差值: 无变化 (不会产生记录)"
                } else {
                    "本次将记录差值: 见人 ${formatDelta(deltaInfo.peopleSeen)} / 查询 ${formatDelta(deltaInfo.queries)} / 成交 ${formatDelta(deltaInfo.deals)}" +
                        if (deltaInfo.hasNegative) " (含下调)" else ""
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (deltaInfo.hasNegative) Color(0xFFE65100) else Color(0xFF2E7D32)
            )
        }

        Spacer(Modifier.height(20.dp))

        fun doSave(allowNegative: Boolean) {
            if (saving) return
            val error = validateDailyEntry(meetText, queryText, dealText)
            if (error != null) {
                scope.launch { snackbarHostState.showSnackbar(error) }
                return
            }
            saving = true
            scope.launch {
                try {
                    val todayKey = DateUtil.dateKey()
                    val isToday = selectedDateKey == todayKey
                    val result = AppContainer.executionRecordService.applyCumulativeInput(
                        dateKey = selectedDateKey,
                        recordTime = if (isToday) System.currentTimeMillis() else null,
                        timePrecision = if (isToday) {
                            ExecutionRecordService.PRECISION_EXACT
                        } else {
                            ExecutionRecordService.PRECISION_DAILY_TOTAL
                        },
                        periodLabel = null,
                        latestPeopleSeen = meetText.trim().toInt(),
                        latestQueries = queryText.trim().toInt(),
                        latestDeals = dealText.trim().toInt(),
                        allowNegative = allowNegative
                    )
                    when (result) {
                        is ExecutionRecordService.CumulativeApplyResult.Saved -> {
                            // 今天的数据变化已在 service 内触发任务/XP/成就刷新
                            snackbarHostState.showSnackbar("已保存, 本次差值 ${formatDeltaTriple(result.delta)}")
                            reloadTick++
                            onDone()
                        }
                        is ExecutionRecordService.CumulativeApplyResult.NoChange -> {
                            snackbarHostState.showSnackbar("数据未变化, 未产生记录")
                            onDone()
                        }
                        is ExecutionRecordService.CumulativeApplyResult.NeedConfirm -> {
                            pendingNegativeDelta = result.delta
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    snackbarHostState.showSnackbar(e.message ?: "保存失败")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("保存失败: ${e.message}")
                } finally {
                    saving = false
                }
            }
        }

        Button(
            onClick = { doSave(false) },
            enabled = !saving,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (saving) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("保存", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // === 数值下调确认 ===
        pendingNegativeDelta?.let { delta ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pendingNegativeDelta = null },
                title = { Text("数值下调确认") },
                text = {
                    DialogScrimAdjuster()
                    Text(
                        "本次录入低于当前累计值, 将记录负差值:\n" +
                            "见人 ${formatDelta(delta.peopleSeen)} / 查询 ${formatDelta(delta.queries)} / 成交 ${formatDelta(delta.deals)}\n\n" +
                            "确认后本条记录会立即写入执行记录, 当天累计将同步下调。"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        pendingNegativeDelta = null
                        doSave(true)
                    }) { Text("确认保存") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingNegativeDelta = null }) { Text("取消") }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        SnackbarHost(snackbarHostState)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtil.utcMillis(selectedDateKey),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                DialogScrimAdjuster()
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDateKey = DateUtil.dateKeyFromUtc(millis)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 数量数据输入校验 (含销售漏斗约束)
 * 规则: 0 <= 成交 <= 查询 <= 见人
 * @return null 表示合法, 否则返回错误提示文案
 */
internal fun validateDailyEntry(meetText: String, queryText: String, dealText: String): String? {
    val inputs = listOf("见人" to meetText, "查询" to queryText, "成交" to dealText)
    for ((label, text) in inputs) {
        if (text.isBlank()) return "${label}不能为空"
        val value = text.trim().toIntOrNull() ?: return "${label}只能输入非负整数"
        if (value < 0) return "${label}不能为负数"
    }
    // 销售漏斗校验: 成交 <= 查询 <= 见人
    val meet = meetText.trim().toInt()
    val query = queryText.trim().toInt()
    val deal = dealText.trim().toInt()
    if (query > meet) return "查询数不能大于见人数"
    if (deal > query) return "成交数不能大于查询数"
    return null
}

@Composable
private fun QuickInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    color: Color,
    suffix: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
        trailingIcon = { Text(suffix) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}
