package com.salesquest.sales_quest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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

/**
 * 执行记录录入面板 — 分段执行记录的核心入口
 *
 * 两种模式:
 * 1. 正常记录 (默认): 3 个输入框 → 保存, App 自动记录当前时间 (EXACT)
 *    无需选择日期/时间, 打开即输入, 保存即完成
 * 2. 历史补录: 选择日期 → 选择时间精度 → 输入数据 → 保存
 *    支持 EXACT (精确时间) / PERIOD (上午下午等) / DAILY_TOTAL (当天总量)
 *    保存后可继续添加下一条, 不自动关闭
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionRecordSheet(
    onDone: () -> Unit,
    isHistorical: Boolean = false,
    initialDateKey: String = DateUtil.dateKey()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDateKey by remember { mutableStateOf(initialDateKey) }
    var meetText by remember { mutableStateOf("") }
    var queryText by remember { mutableStateOf("") }
    var dealText by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    // 历史补录参数
    var timePrecision by remember { mutableStateOf(ExecutionRecordService.PRECISION_EXACT) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(30) }
    var periodLabel by remember { mutableStateOf("上午") }
    var addedCount by remember { mutableStateOf(0) }

    val title = if (isHistorical) "补录执行记录" else "本次执行记录"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (isHistorical && addedCount > 0) {
                Text(
                    "已添加 $addedCount 条",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        if (!isHistorical) {
            // === 正常模式: 只显示提示 ===
            Spacer(Modifier.height(4.dp))
            Text(
                "输入这段时间新增的数据, 保存后自动记录当前时间",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // === 历史补录模式: 日期 + 时间精度 ===
            Spacer(Modifier.height(12.dp))

            // 日期选择
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

            // 时间精度选择
            Text("时间精度", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = timePrecision == ExecutionRecordService.PRECISION_EXACT,
                    onClick = { timePrecision = ExecutionRecordService.PRECISION_EXACT },
                    label = { Text("精确时间") }
                )
                FilterChip(
                    selected = timePrecision == ExecutionRecordService.PRECISION_PERIOD,
                    onClick = { timePrecision = ExecutionRecordService.PRECISION_PERIOD },
                    label = { Text("时段") }
                )
                FilterChip(
                    selected = timePrecision == ExecutionRecordService.PRECISION_DAILY_TOTAL,
                    onClick = { timePrecision = ExecutionRecordService.PRECISION_DAILY_TOTAL },
                    label = { Text("当天总量") }
                )
            }

            Spacer(Modifier.height(12.dp))

            // 根据精度显示不同的时间输入
            when (timePrecision) {
                ExecutionRecordService.PRECISION_EXACT -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = { showTimePicker = true })
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("时间：", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "%02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "选择时间", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                    }
                }
                ExecutionRecordService.PRECISION_PERIOD -> {
                    Text("时段", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("上午", "下午", "晚上").forEach { label ->
                            FilterChip(
                                selected = periodLabel == label,
                                onClick = { periodLabel = label },
                                label = { Text(label) }
                            )
                        }
                    }
                }
                ExecutionRecordService.PRECISION_DAILY_TOTAL -> {
                    Text(
                        "当天总量模式: 只需输入当天总数, 无需时间",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // === 三个数据输入框 ===
        ExecInputField(meetText, { meetText = it }, "见人数", Icons.Filled.Groups, Color(0xFF2196F3), "人")
        Spacer(Modifier.height(10.dp))
        ExecInputField(queryText, { queryText = it }, "查询数", Icons.Filled.Search, Color(0xFF9C27B0), "次")
        Spacer(Modifier.height(10.dp))
        ExecInputField(dealText, { dealText = it }, "成交数", Icons.Filled.Celebration, Color(0xFFF44336), "单")

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (saving) return@Button
                val error = validateExecEntry(meetText, queryText, dealText)
                if (error != null) {
                    scope.launch { snackbarHostState.showSnackbar(error) }
                    return@Button
                }
                saving = true
                scope.launch {
                    try {
                        val meet = meetText.trim().toInt()
                        val query = queryText.trim().toInt()
                        val deal = dealText.trim().toInt()

                        val recordTime: Long?
                        val precision: String
                        val pLabel: String?

                        if (isHistorical) {
                            precision = timePrecision
                            pLabel = when (timePrecision) {
                                ExecutionRecordService.PRECISION_EXACT -> {
                                    val parsed = DateUtil.parseDateKeyToCalendar(selectedDateKey)
                                    val cal = java.util.Calendar.getInstance().apply {
                                        clear()
                                        set(
                                            parsed.get(java.util.Calendar.YEAR),
                                            parsed.get(java.util.Calendar.MONTH),
                                            parsed.get(java.util.Calendar.DAY_OF_MONTH),
                                            selectedHour,
                                            selectedMinute, 0
                                        )
                                    }
                                    recordTime = cal.timeInMillis
                                    null
                                }
                                ExecutionRecordService.PRECISION_PERIOD -> {
                                    recordTime = null
                                    periodLabel
                                }
                                else -> {
                                    recordTime = null
                                    null
                                }
                            }
                        } else {
                            precision = ExecutionRecordService.PRECISION_EXACT
                            recordTime = System.currentTimeMillis()
                            pLabel = null
                        }

                        AppContainer.executionRecordService.addRecord(
                            dateKey = selectedDateKey,
                            recordTime = recordTime,
                            timePrecision = precision,
                            periodLabel = pLabel,
                            peopleSeen = meet,
                            queries = query,
                            deals = deal
                        )

                        if (isHistorical) {
                            // 补录模式: 清空输入, 继续添加
                            meetText = ""
                            queryText = ""
                            dealText = ""
                            addedCount++
                            snackbarHostState.showSnackbar("已保存 (第 $addedCount 条)")
                        } else {
                            snackbarHostState.showSnackbar("已保存")
                            onDone()
                        }
                    } catch (e: IllegalArgumentException) {
                        snackbarHostState.showSnackbar(e.message ?: "保存失败")
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
                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (isHistorical) "保存并继续" else "保存", style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (isHistorical && addedCount > 0) {
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) { Text("完成") }
        }

        Spacer(Modifier.height(20.dp))
        SnackbarHost(snackbarHostState)
    }

    // === 日期选择对话框 ===
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtil.utcMillis(selectedDateKey),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
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

    // === 时间选择对话框 ===
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timePickerState.hour
                    selectedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

/** 执行记录数据输入校验 */
internal fun validateExecEntry(meetText: String, queryText: String, dealText: String): String? {
    val inputs = listOf("见人" to meetText, "查询" to queryText, "成交" to dealText)
    for ((label, text) in inputs) {
        if (text.isBlank()) return "${label}不能为空"
        val value = text.trim().toIntOrNull() ?: return "${label}只能输入非负整数"
        if (value < 0) return "${label}不能为负数"
    }
    return null
}

@Composable
private fun ExecInputField(
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
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
