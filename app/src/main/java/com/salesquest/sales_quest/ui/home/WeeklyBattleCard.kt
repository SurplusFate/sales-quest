package com.salesquest.sales_quest.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salesquest.sales_quest.ui.WeekDayStats

private val MeetColor = Color(0xFF2196F3)
private val QueryColor = Color(0xFF9C27B0)
private val DealColor = Color(0xFFF44336)

/** 本周战绩卡片 - 周一至周六见人/查询/成交折线图 (Canvas 手绘, 无第三方图表库) */
@Composable
fun WeeklyBattleCard(weekStats: List<WeekDayStats>) {
    var selectedDay by remember { mutableStateOf<WeekDayStats?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            "本周战绩",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        WeekLineChart(
            weekStats = weekStats,
            onDayClick = { selectedDay = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        Spacer(Modifier.height(8.dp))
        WeekChartLegend()
    }

    selectedDay?.let { day ->
        AlertDialog(
            onDismissRequest = { selectedDay = null },
            title = { Text("${day.weekday} · ${day.dateLabel}") },
            text = {
                Column {
                    LegendRow("见人", day.stats.peopleSeen, MeetColor)
                    Spacer(Modifier.height(8.dp))
                    LegendRow("查询", day.stats.queries, QueryColor)
                    Spacer(Modifier.height(8.dp))
                    LegendRow("成交", day.stats.deals, DealColor)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDay = null }) { Text("关闭") }
            }
        )
    }
}

/** 图例 */
@Composable
private fun WeekChartLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendDot("见人", MeetColor)
        LegendDot("查询", QueryColor)
        LegendDot("成交", DealColor)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(10.dp)
                .height(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LegendRow(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(10.dp)
                .height(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

/** 折线图主体 */
@Composable
private fun WeekLineChart(
    weekStats: List<WeekDayStats>,
    onDayClick: (WeekDayStats) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val axisLabelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val dayLabelStyle = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)

    val values = weekStats
    val maxValue = (values.maxOfOrNull { listOf(it.stats.peopleSeen, it.stats.queries, it.stats.deals).max() } ?: 0)
        .coerceAtLeast(1)

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    val chartModifier = modifier.pointerInput(values) {
        detectTapGestures { offset ->
            val leftPad = 28.dp.toPx()
            val rightPad = 8.dp.toPx()
            val plotWidth = size.width - leftPad - rightPad
            val pointCount = values.size
            if (pointCount < 2 || plotWidth <= 0f) return@detectTapGestures
            val xStep = plotWidth / (pointCount - 1)
            val rawIndex = (offset.x - leftPad) / xStep
            val index = (rawIndex + 0.5f).toInt().coerceIn(0, pointCount - 1)
            values.getOrNull(index)?.let(onDayClick)
        }
    }

    Canvas(modifier = chartModifier) {
        val chartWidth = size.width
        val chartHeight = size.height
        val leftPad = 28.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 8.dp.toPx()
        val bottomPad = 22.dp.toPx()

        val plotWidth = chartWidth - leftPad - rightPad
        val plotHeight = chartHeight - topPad - bottomPad

        val pointCount = values.size
        if (pointCount < 2) return@Canvas

        val xStep = plotWidth / (pointCount - 1)
        fun xFor(index: Int): Float = leftPad + xStep * index
        fun yFor(v: Int): Float = topPad + plotHeight - (v.toFloat() / maxValue) * plotHeight

        // 横向网格线 + y 轴刻度
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = topPad + plotHeight * i / gridLines
            val labelValue = maxValue * (gridLines - i) / gridLines
            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + plotWidth, y),
                strokeWidth = 1f
            )
            drawText(
                textMeasurer = textMeasurer,
                text = labelValue.toString(),
                topLeft = Offset(0f, y - 6f),
                style = axisLabelStyle
            )
        }

        // 绘制三条折线 + 数据点
        fun drawSeries(statsOf: (WeekDayStats) -> Int, color: Color) {
            val path = Path()
            values.forEachIndexed { index, day ->
                val x = xFor(index)
                val y = yFor(statsOf(day))
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            values.forEachIndexed { index, day ->
                val x = xFor(index)
                val y = yFor(statsOf(day))
                drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(x, y))
            }
        }
        drawSeries({ it.stats.peopleSeen }, MeetColor)
        drawSeries({ it.stats.queries }, QueryColor)
        drawSeries({ it.stats.deals }, DealColor)

        // x 轴底部标签: 周几 + 日期
        val dayWidth = plotWidth / pointCount
        clipRect(right = chartWidth) {
            values.forEachIndexed { index, day ->
                val cx = xFor(index)
                val text = "${day.weekday} ${day.dateLabel}"
                val layout = textMeasurer.measure(text, dayLabelStyle)
                val textWidth = layout.size.width.toFloat()
                val textLeft = (cx - textWidth / 2f)
                    .coerceIn(leftPad, chartWidth - rightPad - textWidth)
                drawText(
                    textMeasurer = textMeasurer,
                    text = text,
                    topLeft = Offset(textLeft, chartHeight - bottomPad + 2f),
                    style = dayLabelStyle
                )
            }
        }
    }
}
