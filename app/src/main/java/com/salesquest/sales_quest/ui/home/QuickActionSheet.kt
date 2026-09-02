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
import kotlinx.coroutines.launch

/**
 * 每日基础任务数据录入面板 - 直接输入当天实际数值
 *
 * 支持:
 * - 整组输入见人/查询/成交 + 保存
 * - 选择历史日期补录/修改
 * - 已有数据再次编辑
 * - 保存后立即刷新首页/本周折线图
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

    // 切换日期时加载该日期已有数据
    LaunchedEffect(selectedDateKey) {
        val stats = AppContainer.dailyStatsService.getDailyStats(selectedDateKey)
        meetText = stats.peopleSeen.toString()
        queryText = stats.queries.toString()
        dealText = stats.deals.toString()
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

        QuickInputField(meetText, { meetText = it }, "见人数", Icons.Filled.Groups, Color(0xFF2196F3), "人")
        Spacer(Modifier.height(12.dp))
        QuickInputField(queryText, { queryText = it }, "查询数", Icons.Filled.Search, Color(0xFF9C27B0), "次")
        Spacer(Modifier.height(12.dp))
        QuickInputField(dealText, { dealText = it }, "成交数", Icons.Filled.Celebration, Color(0xFFF44336), "单")

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (saving) return@Button
                val error = validateDailyEntry(meetText, queryText, dealText)
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
                        val todayKey = DateUtil.dateKey()
                        if (selectedDateKey == todayKey) {
                            // 今天: 先整组写入 (内部 updateDailyStats 校验漏斗), 再统一触发任务/XP/成就
                            // 避免逐项 setPeopleSeen/setQuery/setDeal 的中间态校验导致下调数据保存失败
                            AppContainer.dailyStatsService.updateDailyStats(todayKey, meet, query, deal)
                            AppContainer.quickActionService.refreshAfterDataChange()
                        } else {
                            // 历史日期: 纯数据补录/修改, 不触发 XP
                            AppContainer.dailyStatsService.updateDailyStats(selectedDateKey, meet, query, deal)
                        }
                        snackbarHostState.showSnackbar("已保存")
                        onDone()
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
                Text("保存", style = MaterialTheme.typography.bodyLarge)
            }
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
