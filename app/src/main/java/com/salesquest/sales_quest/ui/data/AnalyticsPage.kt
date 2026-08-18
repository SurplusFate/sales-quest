package com.salesquest.sales_quest.ui.data

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.data.DateUtil
import kotlinx.coroutines.launch

/** 数据分析页 - 任意历史日期查看/录入/修改 + 累计数据 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsPage(viewModel: AnalyticsViewModel = viewModel()) {
    val today by viewModel.today.collectAsState()
    val total by viewModel.total.collectAsState()
    val selectedStats by viewModel.selectedStats.collectAsState()
    val executionRate by viewModel.executionRate.collectAsState()
    var tabIndex by remember { mutableStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var editRequest by remember { mutableStateOf<EditMetricRequest?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectedDateKey = remember { mutableStateOf(DateUtil.dateKey()) }

    val isToday = tabIndex == 0
    val people = if (isToday) selectedStats.peopleSeen else total.totalMeet
    val queries = if (isToday) selectedStats.queries else total.totalQuery
    val deals = if (isToday) selectedStats.deals else total.totalDeal

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据分析") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 16.dp)
        ) {
            // === 时间切换 ===
            TimeToggle(selectedIndex = tabIndex) { tabIndex = it }

            if (isToday) {
                Spacer(Modifier.height(12.dp))
                // === 日期选择器 ===
                DateSelector(
                    dateKey = selectedDateKey.value,
                    onClick = { showDatePicker = true }
                )
                Spacer(Modifier.height(12.dp))
                if (selectedDateKey.value == DateUtil.dateKey()) {
                    ExecutionRateCard(rate = executionRate)
                    Spacer(Modifier.height(12.dp))
                }
            }

            // === 核心数据卡片 (点击数字编辑) ===
            CoreStatsRow(
                people = people,
                queries = queries,
                deals = deals,
                editable = isToday,
                onEditPeople = { editRequest = EditMetricRequest("见人数", people, "人", "MEET", selectedDateKey.value) },
                onEditQueries = { editRequest = EditMetricRequest("查询数", queries, "次", "QUERY", selectedDateKey.value) },
                onEditDeals = { editRequest = EditMetricRequest("成交数", deals, "单", "DEAL", selectedDateKey.value) }
            )
            Spacer(Modifier.height(16.dp))

            // === 转化率区域 ===
            SectionTitle("转化率")
            Spacer(Modifier.height(8.dp))
            RateTile("查询率", "查询 ÷ 见人", queries, people)
            RateTile("成交率", "成交 ÷ 见人", deals, people)
            RateTile("查询成交率", "成交 ÷ 查询", deals, queries)

            // === 累计数据区域 (仅今日视图显示) ===
            if (isToday) {
                Spacer(Modifier.height(16.dp))
                SectionTitle("累计数据")
                Spacer(Modifier.height(8.dp))
                CoreStatsRow(people = total.totalMeet, queries = total.totalQuery, deals = total.totalDeal)
            }
        }
    }

    // === 日期选择对话框 ===
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtil.utcMillis(selectedDateKey.value),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val key = DateUtil.dateKeyFromUtc(millis)
                        selectedDateKey.value = key
                        viewModel.selectDate(key)
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

    // === 编辑对话框 ===
    editRequest?.let { req ->
        EditMetricDialog(
            request = req,
            onDismiss = { editRequest = null },
            onSave = { v ->
                scope.launch {
                    try {
                        viewModel.editDailyMetric(req.dateKey, req.metricCode, v)
                        editRequest = null
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
}

/** 日期选择入口 */
@Composable
fun DateSelector(dateKey: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("日期：", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(dateKey, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.ArrowDropDown, contentDescription = "选择日期", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal data class EditMetricRequest(
    val label: String,
    val currentValue: Int,
    val suffix: String,
    val metricCode: String,
    val dateKey: String
)

@Composable
internal fun EditMetricDialog(
    request: EditMetricRequest,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var text by remember { mutableStateOf(request.currentValue.toString()) }

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
                onClick = {
                    val parsed = text.trim().toIntOrNull() ?: -1
                    if (parsed >= 0) onSave(parsed)
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/** 时间切换条 (V1 仅实现 今日 / 累计) */
@Composable
fun TimeToggle(selectedIndex: Int, onChanged: (Int) -> Unit) {
    val labels = listOf("今日", "本周", "本月", "累计")
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        labels.forEachIndexed { i, label ->
            FilterChip(
                selected = i == selectedIndex,
                onClick = { if (i == 0 || i == 3) onChanged(i) },
                label = { Text(label) }
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}

/** 今日执行度进度卡片 */
@Composable
fun ExecutionRateCard(rate: Double) {
    val percent = (rate * 100).coerceIn(0.0, 100.0).toInt()
    val color = if (rate >= 0.8) Color(0xFF4CAF50)
    else if (rate >= 0.5) Color(0xFFFF9800)
    else Color(0xFFF44336)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("今日执行度", style = MaterialTheme.typography.labelLarge)
                Text("$percent%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { rate.coerceIn(0.0, 1.0).toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

/** 核心数据横排 (见人 / 查询 / 成交) */
@Composable
fun CoreStatsRow(
    people: Int,
    queries: Int,
    deals: Int,
    editable: Boolean = false,
    onEditPeople: () -> Unit = {},
    onEditQueries: () -> Unit = {},
    onEditDeals: () -> Unit = {}
) {
    Row {
        StatCell(
            label = "见人",
            value = people,
            color = Color(0xFF2196F3),
            icon = Icons.Filled.Groups,
            modifier = Modifier.weight(1f),
            editable = editable,
            onClick = onEditPeople
        )
        Spacer(Modifier.width(8.dp))
        StatCell(
            label = "查询",
            value = queries,
            color = Color(0xFF9C27B0),
            icon = Icons.Filled.Search,
            modifier = Modifier.weight(1f),
            editable = editable,
            onClick = onEditQueries
        )
        Spacer(Modifier.width(8.dp))
        StatCell(
            label = "成交",
            value = deals,
            color = Color(0xFFF44336),
            icon = Icons.Filled.Celebration,
            modifier = Modifier.weight(1f),
            editable = editable,
            onClick = onEditDeals
        )
    }
}

/** 单个核心数字格 */
@Composable
fun StatCell(
    label: String,
    value: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .padding(vertical = 14.dp, horizontal = 8.dp)
            .let { if (editable) it.clickable(onClick = onClick) else it },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

/** 转化率条目 (分母为 0 时显示 "暂无数据") */
@Composable
fun RateTile(label: String, formula: String, numerator: Int, denominator: Int) {
    val valueText: String
    val valueColor: Color
    val subtitle: String

    if (denominator == 0) {
        valueText = "暂无数据"
        valueColor = MaterialTheme.colorScheme.outline
        subtitle = formula
    } else {
        val rate = numerator.toDouble() / denominator
        valueText = String.format("%.1f%%", rate * 100)
        valueColor = if (rate >= 0.5) Color(0xFF4CAF50)
        else if (rate >= 0.2) Color(0xFFFF9800)
        else Color(0xFFF44336)
        subtitle = "$numerator ÷ $denominator"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(valueText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
