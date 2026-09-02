package com.salesquest.sales_quest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.FunnelValidator
import com.salesquest.sales_quest.ui.ExecutionRecordUi
import kotlinx.coroutines.launch

/**
 * 执行记录列表页 — 查看某天的全部执行记录 + 编辑/删除/补录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionRecordListPage(
    onBack: () -> Unit = {},
    viewModel: ExecutionRecordViewModel = viewModel()
) {
    val records by viewModel.records.collectAsState()
    val dailyTotal by viewModel.dailyTotal.collectAsState()
    val selectedDateKey by viewModel.selectedDateKey.collectAsState()
    val allDates by viewModel.allDates.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var editRecord by remember { mutableStateOf<ExecutionRecordUi?>(null) }
    var deleteRecord by remember { mutableStateOf<ExecutionRecordUi?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("执行记录") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 24.dp)
        ) {
            // === 日期选择 ===
            DateSelectorBar(
                dateKey = selectedDateKey,
                onClick = { showDatePicker = true }
            )

            Spacer(Modifier.height(12.dp))

            // === 当天累计 ===
            DailyTotalCard(
                people = dailyTotal.peopleSeen,
                queries = dailyTotal.queries,
                deals = dailyTotal.deals,
                recordCount = records.size
            )

            Spacer(Modifier.height(16.dp))

            // === 记录列表 ===
            if (records.isEmpty()) {
                EmptyRecordsCard()
            } else {
                records.forEach { record ->
                    ExecutionRecordRow(
                        record = record,
                        onEdit = { editRecord = record },
                        onDelete = { deleteRecord = record }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // === 添加按钮 ===
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("添加执行记录")
            }

            // === 历史日期 ===
            if (allDates.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "历史日期",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allDates) { date ->
                        DateChip(
                            dateKey = date,
                            isSelected = date == selectedDateKey,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }
            }
        }
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
                        viewModel.selectDate(DateUtil.dateKeyFromUtc(millis))
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

    // === 添加执行记录 (历史补录模式) ===
    if (showAddSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState
        ) {
            val isHistorical = selectedDateKey != DateUtil.dateKey()
            ExecutionRecordSheet(
                onDone = { showAddSheet = false },
                isHistorical = true,
                initialDateKey = selectedDateKey
            )
        }
    }

    // === 编辑对话框 ===
    editRecord?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { editRecord = null },
            onSave = { meet, query, deal ->
                scope.launch {
                    viewModel.updateRecord(record.id, meet, query, deal) { ok, msg ->
                        if (ok) {
                            editRecord = null
                            scope.launch { snackbarHostState.showSnackbar("已修改") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("修改失败: $msg") }
                        }
                    }
                }
            }
        )
    }

    // === 删除确认 ===
    deleteRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { deleteRecord = null },
            title = { Text("删除记录") },
            text = {
                Text("删除 ${record.timeLabel} 的记录?\n见人 +${record.peopleSeen}  查询 +${record.queries}  成交 +${record.deals}")
            },
            confirmButton = {
                TextButton(onClick = {
                    val r = record
                    deleteRecord = null
                    scope.launch {
                        viewModel.deleteRecord(r.id) { ok, msg ->
                            if (ok) {
                                scope.launch { snackbarHostState.showSnackbar("已删除") }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("删除失败: $msg") }
                            }
                        }
                    }
                }) { Text("删除", color = Color(0xFFF44336)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteRecord = null }) { Text("取消") }
            }
        )
    }
}

/** 日期选择入口 */
@Composable
private fun DateSelectorBar(dateKey: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("日期：", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(dateKey, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.ArrowDropDown, contentDescription = "选择日期", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        if (dateKey == DateUtil.dateKey()) {
            Text(
                "今天",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 当天累计卡片 */
@Composable
private fun DailyTotalCard(people: Int, queries: Int, deals: Int, recordCount: Int) {
    val theme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("当天累计", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("$recordCount 条记录", style = MaterialTheme.typography.labelSmall, color = theme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TotalCell("见人", people, Color(0xFF2196F3))
                TotalCell("查询", queries, Color(0xFF9C27B0))
                TotalCell("成交", deals, Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun TotalCell(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

/** 单条执行记录行 */
@Composable
private fun ExecutionRecordRow(
    record: ExecutionRecordUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        record.timeLabel.take(2),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    record.timeLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (record.timePrecision != "EXACT") {
                    PrecisionBadge(record.timePrecision)
                    Spacer(Modifier.width(6.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp), tint = Color(0xFFF44336).copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DeltaCell("见人", "+${record.peopleSeen}", Color(0xFF2196F3))
                DeltaCell("查询", "+${record.queries}", Color(0xFF9C27B0))
                DeltaCell("成交", "+${record.deals}", Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun PrecisionBadge(precision: String) {
    val label = when (precision) {
        "PERIOD" -> "时段"
        "DAILY_TOTAL" -> "总量"
        else -> ""
    }
    if (label.isNotEmpty()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFF9800).copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
        }
    }
}

@Composable
private fun DeltaCell(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyRecordsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("暂无执行记录", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("点击下方按钮添加", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun DateChip(dateKey: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow
    val fg = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            DateUtil.monthDayLabel(dateKey),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
        Text(
            DateUtil.weekdayName(dateKey),
            style = MaterialTheme.typography.labelSmall,
            color = fg.copy(alpha = 0.7f)
        )
    }
}

/** 编辑记录对话框 (3 个字段) */
@Composable
private fun EditRecordDialog(
    record: ExecutionRecordUi,
    onDismiss: () -> Unit,
    onSave: (meet: Int, query: Int, deal: Int) -> Unit
) {
    var meetText by remember { mutableStateOf(record.peopleSeen.toString()) }
    var queryText by remember { mutableStateOf(record.queries.toString()) }
    var dealText by remember { mutableStateOf(record.deals.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改 ${record.timeLabel} 记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = meetText,
                    onValueChange = { meetText = it; errorText = null },
                    label = { Text("见人数") },
                    trailingIcon = { Text("人") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it; errorText = null },
                    label = { Text("查询数") },
                    trailingIcon = { Text("次") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dealText,
                    onValueChange = { dealText = it; errorText = null },
                    label = { Text("成交数") },
                    trailingIcon = { Text("单") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(
                        errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val meet = meetText.trim().toIntOrNull() ?: -1
                val query = queryText.trim().toIntOrNull() ?: -1
                val deal = dealText.trim().toIntOrNull() ?: -1
                if (meet >= 0 && query >= 0 && deal >= 0) {
                    errorText = FunnelValidator.errorOrNull(meet, query, deal)
                    if (errorText == null) {
                        onSave(meet, query, deal)
                    }
                } else {
                    errorText = "只能输入非负整数"
                }
            }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
