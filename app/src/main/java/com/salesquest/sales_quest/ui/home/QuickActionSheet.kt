package com.salesquest.sales_quest.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.salesquest.sales_quest.core.AppContainer
import kotlinx.coroutines.launch

/** 快速记录面板 - 三个直接输入框 + 一个保存按钮 */
@Composable
fun QuickActionSheet(
    onDone: () -> Unit,
    initial: Triple<Int, Int, Int> = Triple(0, 0, 0)
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var meetText by remember { mutableStateOf(initial.first.toString()) }
    var queryText by remember { mutableStateOf(initial.second.toString()) }
    var dealText by remember { mutableStateOf(initial.third.toString()) }
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            "快速记录",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        QuickInputField(meetText, { meetText = it }, "见人数", Icons.Filled.Groups, Color(0xFF2196F3), "人")
        Spacer(Modifier.height(12.dp))
        QuickInputField(queryText, { queryText = it }, "查询数", Icons.Filled.Search, Color(0xFF9C27B0), "次")
        Spacer(Modifier.height(12.dp))
        QuickInputField(dealText, { dealText = it }, "成交数", Icons.Filled.Celebration, Color(0xFFF44336), "单")

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (saving) return@Button
                saving = true
                scope.launch {
                    try {
                        val meet = meetText.trim().toIntOrNull() ?: 0
                        val query = queryText.trim().toIntOrNull() ?: 0
                        val deal = dealText.trim().toIntOrNull() ?: 0
                        if (meet < 0 || query < 0 || deal < 0) {
                            snackbarHostState.showSnackbar("数字不能为负数")
                            return@launch
                        }
                        val service = AppContainer.quickActionService
                        service.setPeopleSeen(meet)
                        service.setQuery(query)
                        service.setDeal(deal)
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
    }
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
