package com.salesquest.sales_quest.ui.data

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
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** 数据分析页 - V1.0 重构 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsPage(viewModel: AnalyticsViewModel = viewModel()) {
    val today by viewModel.today.collectAsState()
    val total by viewModel.total.collectAsState()
    val executionRate by viewModel.executionRate.collectAsState()
    var tabIndex by remember { mutableStateOf(0) }

    val isToday = tabIndex == 0
    val people = if (isToday) today.peopleSeen else total.totalMeet
    val queries = if (isToday) today.queries else total.totalQuery
    val deals = if (isToday) today.deals else total.totalDeal

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("数据分析") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
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
                ExecutionRateCard(rate = executionRate)
                Spacer(Modifier.height(12.dp))
            }

            // === 核心数据卡片 ===
            CoreStatsRow(people = people, queries = queries, deals = deals)
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
fun CoreStatsRow(people: Int, queries: Int, deals: Int) {
    Row {
        StatCell(label = "见人", value = people, color = Color(0xFF2196F3), icon = Icons.Filled.Groups, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        StatCell(label = "查询", value = queries, color = Color(0xFF9C27B0), icon = Icons.Filled.Search, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        StatCell(label = "成交", value = deals, color = Color(0xFFF44336), icon = Icons.Filled.Celebration, modifier = Modifier.weight(1f))
    }
}

/** 单个核心数字格 */
@Composable
fun StatCell(label: String, value: Int, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 14.dp, horizontal = 8.dp),
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
