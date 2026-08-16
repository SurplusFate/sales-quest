package com.salesquest.sales_quest.ui.customers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.DateUtil
import com.salesquest.sales_quest.data.Operator
import com.salesquest.sales_quest.data.entity.CustomerEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/** 客户详情页 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailPage(
    customerId: String,
    onBack: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    viewModel: CustomerDetailViewModel = viewModel(factory = CustomerDetailViewModel.factory(customerId))
) {
    val customer by viewModel.customer.collectAsState()
    val followUps by viewModel.followUps.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(customer?.name ?: "客户详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(customerId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "编辑")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (customer == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("客户不存在")
            }
        } else {
            val c = customer!!
            DetailBody(
                customer = c,
                followUps = followUps,
                onDelete = { showDeleteConfirm = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除客户") },
            text = { Text("确认删除此客户？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            try {
                                AppContainer.deleteCustomer(customerId)
                                onBack()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("删除失败: ${e.message}")
                            }
                        }
                    }
                ) { Text("删除", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
}

/** 详情内容 */
@Composable
fun DetailBody(
    customer: CustomerEntity,
    followUps: List<com.salesquest.sales_quest.data.entity.FollowUpEntity>,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val operator = Operator.fromCode(customer.operator)
    val stage = CustomerStage.fromCode(customer.salesStage)
    val hasPackage = customer.actualCost != null ||
        (customer.packageName != null && customer.packageName.isNotEmpty()) ||
        (customer.traffic != null && customer.traffic.isNotEmpty()) ||
        (customer.minutes != null && customer.minutes.isNotEmpty()) ||
        customer.broadband || customer.camera || customer.subCards > 0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
    ) {
        item {
            InfoCard(title = "基础信息") {
                InfoRow("称呼", customer.name)
                if (customer.phone.isNotEmpty()) InfoRow("手机号", customer.phone)
                InfoRow("运营商", operator.label)
                InfoRow("月消费", costLabel(customer.selfReportedCost))
                InfoRow("状态", stage.label)
            }
        }

        if (hasPackage) {
            item {
                Spacer(Modifier.height(12.dp))
                InfoCard(title = "套餐详情") {
                    if (customer.packageName != null && customer.packageName.isNotEmpty()) {
                        InfoRow("套餐名称", customer.packageName)
                    }
                    if (customer.actualCost != null) InfoRow("实际消费", "${customer.actualCost}元")
                    if (customer.traffic != null && customer.traffic.isNotEmpty()) InfoRow("流量", customer.traffic)
                    if (customer.minutes != null && customer.minutes.isNotEmpty()) InfoRow("通话", customer.minutes)
                    InfoRow("宽带", if (customer.broadband) "有" else "无")
                    InfoRow("摄像头", if (customer.camera) "有" else "无")
                    InfoRow("副卡", if (customer.subCards > 0) "${customer.subCards}张" else "无")
                }
            }
        }

        if (customer.note != null && customer.note.isNotEmpty()) {
            item {
                Spacer(Modifier.height(12.dp))
                InfoCard(title = "备注") {
                    Text(customer.note, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            InfoCard(title = "跟进记录") {
                if (followUps.isEmpty()) {
                    Text("暂无跟进记录", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    followUps.forEach { f ->
                        FollowUpTile(followUp = f)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = Color.Red)
                Spacer(Modifier.size(8.dp))
                Text("删除客户", color = Color.Red)
            }
        }
    }
}

@Composable
fun FollowUpTile(followUp: com.salesquest.sales_quest.data.entity.FollowUpEntity) {
    val timeStr = formatTime(followUp.scheduledAt)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (followUp.completed) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
            contentDescription = null,
            tint = if (followUp.completed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                followUp.content ?: "跟进",
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (followUp.completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (followUp.completed) Color.Gray else Color.Unspecified
            )
            Text(timeStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private fun costLabel(cost: Int?): String = if (cost == null) "不清楚" else "${cost}元"

private fun formatTime(ts: Long): String {
    val sdf = SimpleDateFormat("M/d HH:mm", Locale.getDefault())
    return sdf.format(java.util.Date(ts))
}
