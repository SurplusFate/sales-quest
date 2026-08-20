package com.salesquest.sales_quest.ui.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.services.DailySummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 总结页 - 每日总结 + 周期总结
 *
 * 每日总结: 自动显示当天核心数据摘要 + 五个字段 + 日期绑定 + 历史列表
 * 周期总结: 本周 vs 上周对比 + 简单规则分析
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryPage(
    onBack: () -> Unit = {},
    viewModel: SummaryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("总结") },
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
                .padding(16.dp)
        ) {
            // === 每日总结 ===
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "每日总结",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text(state.form.dateKey)
                }
            }
            Spacer(Modifier.height(8.dp))

            // 数据摘要
            state.snapshot?.let { snap ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "${snap.dateKey} 数据摘要",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth()) {
                            StatItem("见人", snap.peopleSeen, Modifier.weight(1f))
                            StatItem("查询", snap.queries, Modifier.weight(1f))
                            StatItem("成交", snap.deals, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "查询率: ${String.format("%.1f%%", snap.queryRate)}   " +
                                "成交率: ${String.format("%.1f%%", snap.dealRate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.form.good,
                onValueChange = viewModel::onGoodChange,
                label = { Text("今日做得好的地方") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.form.problems,
                onValueChange = viewModel::onProblemsChange,
                label = { Text("今日遇到的问题") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.form.customerFeedback,
                onValueChange = viewModel::onFeedbackChange,
                label = { Text("今日客户反馈") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.form.discovery,
                onValueChange = viewModel::onDiscoveryChange,
                label = { Text("今日最大的发现") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.form.improvement,
                onValueChange = viewModel::onImprovementChange,
                label = { Text("明天准备改进什么") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth()
            ) { Text("保存总结") }

            // === 周期总结 ===
            state.weekComparison?.let { comp ->
                Spacer(Modifier.height(20.dp))
                Text(
                    "周期总结",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("本周 vs 上周", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text("本周", style = MaterialTheme.typography.labelSmall)
                                Text("见人 ${comp.current.peopleSeen}", style = MaterialTheme.typography.bodySmall)
                                Text("查询 ${comp.current.queries}", style = MaterialTheme.typography.bodySmall)
                                Text("成交 ${comp.current.deals}", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("上周", style = MaterialTheme.typography.labelSmall)
                                Text("见人 ${comp.previous.peopleSeen}", style = MaterialTheme.typography.bodySmall)
                                Text("查询 ${comp.previous.queries}", style = MaterialTheme.typography.bodySmall)
                                Text("成交 ${comp.previous.deals}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            comp.analysis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // === 历史总结 ===
            // 性能优化: 提取为独立 Composable, 文本输入时不会重组历史列表
            HistorySection(history = state.history)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtil.utcMillis(state.form.dateKey)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val picked = datePickerState.selectedDateMillis
                    if (picked != null) {
                        viewModel.selectDate(DateUtil.dateKeyFromUtc(picked))
                    }
                    showDatePicker = false
                }) { Text("确定" ) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

/** 历史总结区域 — 独立 Composable, 文本输入时不会重组 */
@Composable
private fun HistorySection(history: List<DailySummary>) {
    if (history.isEmpty()) return
    Spacer(Modifier.height(20.dp))
    Text(
        "历史总结",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(8.dp))
    history.forEach { summary ->
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text(summary.dateKey, style = MaterialTheme.typography.titleSmall)
                if (summary.good.isNotBlank()) {
                    Text("做得好的: ${summary.good}", style = MaterialTheme.typography.bodySmall)
                }
                if (summary.improvement.isNotBlank()) {
                    Text("改进: ${summary.improvement}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
