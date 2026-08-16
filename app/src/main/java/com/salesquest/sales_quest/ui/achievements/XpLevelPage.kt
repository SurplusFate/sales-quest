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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.core.LevelDef

/** 等级详情页 - 当前等级大圆 + XP 进度条 + 全部等级路线 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XpLevelPage(
    onBack: () -> Unit = {},
    viewModel: XpLevelViewModel = viewModel()
) {
    val totalXp by viewModel.totalXp.collectAsState()

    val currentLevel = AppLevels.getLevel(totalXp)
    val nextLevel = AppLevels.getNextLevel(totalXp)
    val progress = AppLevels.getProgress(totalXp)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("等级") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                CurrentLevelCard(
                    level = currentLevel,
                    nextLevel = nextLevel,
                    totalXp = totalXp,
                    progress = progress
                )
            }
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    "等级路线",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }
            items(AppLevels.levels, key = { it.level }) { lv ->
                LevelRow(
                    lv = lv,
                    currentLevel = currentLevel,
                    totalXp = totalXp
                )
            }
        }
    }
}

/** 当前等级卡片 */
@Composable
fun CurrentLevelCard(
    level: LevelDef,
    nextLevel: LevelDef?,
    totalXp: Int,
    progress: Double
) {
    val theme = MaterialTheme.colorScheme
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(theme.primary, theme.tertiary))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${level.level}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                    style = MaterialTheme.typography.displaySmall
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                level.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "$totalXp XP",
                style = MaterialTheme.typography.titleMedium,
                color = theme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            if (nextLevel != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$totalXp XP", style = MaterialTheme.typography.labelSmall)
                    Text("${nextLevel.xpRequired} XP", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    trackColor = theme.surfaceContainerHighest
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "距 ${nextLevel.title} 还需 ${nextLevel.xpRequired - totalXp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFC107).copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "已达最高等级!",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** 单个等级条目 */
@Composable
fun LevelRow(lv: LevelDef, currentLevel: LevelDef, totalXp: Int) {
    val theme = MaterialTheme.colorScheme
    val isCurrent = currentLevel.level == lv.level
    val reached = totalXp >= lv.xpRequired

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        colors = if (isCurrent) {
            androidx.compose.material3.CardDefaults.cardColors(
                containerColor = theme.primaryContainer.copy(alpha = 0.4f)
            )
        } else {
            androidx.compose.material3.CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (reached) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (reached) Color(0xFFFFC107) else theme.outline,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Lv.${lv.level} ${lv.title}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (reached) Color.Unspecified else theme.outline
                )
                Text(
                    "${lv.xpRequired} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.onSurfaceVariant
                )
            }
            when {
                isCurrent -> {
                    AssistChip(
                        onClick = {},
                        label = { Text("当前") }
                    )
                }
                reached -> {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
