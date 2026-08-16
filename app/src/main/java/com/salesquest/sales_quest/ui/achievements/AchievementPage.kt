package com.salesquest.sales_quest.ui.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.services.AchievementStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 成就页 - 2 列 Grid 展示所有成就, 底部等级详情入口 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementPage(
    onOpenXpLevel: () -> Unit = {},
    viewModel: AchievementViewModel = viewModel()
) {
    val statuses by viewModel.statuses.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("成就") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        if (statuses.isEmpty()) {
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
            ) {
                // === 成就统计 ===
                val unlockedCount = statuses.count { it.unlocked }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "成就",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "$unlockedCount / ${statuses.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // === 成就网格 ===
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 12.dp, top = 4.dp, end = 12.dp, bottom = 12.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(statuses, key = { it.def.id }) { status ->
                        AchievementGridCard(status = status)
                    }
                }

                // === 底部等级入口 ===
                FilledButton(
                    onClick = onOpenXpLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.MilitaryTech, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("等级详情")
                }
            }
        }
    }
}

/** 单个成就卡片 */
@Composable
fun AchievementGridCard(status: AchievementStatus) {
    val theme = MaterialTheme.colorScheme
    val def = status.def
    val unlocked = status.unlocked

    val bg = if (unlocked) theme.primaryContainer.copy(alpha = 0.5f) else theme.surfaceContainerLow
    val titleColor = if (unlocked) theme.onPrimaryContainer else theme.outline
    val descColor = if (unlocked) theme.onSurfaceVariant else theme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(def.icon, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                Spacer(Modifier.weight(1f))
                Icon(
                    if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (unlocked) Color(0xFF4CAF50) else theme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                def.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                def.description,
                style = MaterialTheme.typography.labelSmall,
                color = descColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            if (unlocked && status.unlockedAt != null) {
                Text(
                    formatDate(status.unlockedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    "未解锁",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.outline
                )
            }
        }
    }
}

private fun formatDate(ts: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return "${sdf.format(Date(ts))} 解锁"
}
