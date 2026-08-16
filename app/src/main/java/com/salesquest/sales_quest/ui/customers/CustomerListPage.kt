package com.salesquest.sales_quest.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.Operator
import com.salesquest.sales_quest.data.entity.CustomerEntity

/** 客户列表页 - 只展示值得跟进的客户 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListPage(
    onAddCustomer: () -> Unit = {},
    onOpenCustomer: (String) -> Unit = {},
    viewModel: CustomerListViewModel = viewModel()
) {
    val customers by viewModel.customers.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("客户") },
                actions = {
                    IconButton(onClick = onAddCustomer) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "添加客户")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        val worthFollowing = customers.filter {
            CustomerStage.fromCode(it.salesStage) != CustomerStage.WON
        }

        if (worthFollowing.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.PeopleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("还没有客户, 点击右上角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(worthFollowing, key = { it.id }) { customer ->
                    CustomerTile(customer = customer, onClick = { onOpenCustomer(customer.id) })
                }
            }
        }
    }
}

/** 单个客户列表项 */
@Composable
fun CustomerTile(customer: CustomerEntity, onClick: () -> Unit) {
    val theme = MaterialTheme.colorScheme
    val operator = Operator.fromCode(customer.operator)
    val stage = CustomerStage.fromCode(customer.salesStage)
    val cost = customer.actualCost ?: customer.selfReportedCost
    val initial = if (customer.name.isEmpty()) "?" else customer.name.first().toString()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, color = theme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Tag(label = operator.label, color = operatorColor(operator))
                    Spacer(Modifier.width(6.dp))
                    Tag(label = stage.label, color = stageColor(stage))
                    if (cost != null) {
                        Spacer(Modifier.width(6.dp))
                        Tag(label = "$cost元", color = theme.primary)
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = theme.outline)
        }
    }
}

/** 小标签 (Material 3 tonal 风格) */
@Composable
fun Tag(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = MaterialTheme.typography.labelSmall.fontSize, color = color, fontWeight = FontWeight.SemiBold)
    }
}

fun operatorColor(op: Operator): Color = when (op) {
    Operator.MOBILE -> Color(0xFF2196F3)
    Operator.UNICOM -> Color(0xFFF44336)
    Operator.TELECOM -> Color(0xFF009688)
    Operator.UNKNOWN -> Color(0xFF607D8B)
}

fun stageColor(stage: CustomerStage): Color = when (stage) {
    CustomerStage.NEW -> Color(0xFF607D8B)
    CustomerStage.CONTACTED -> Color(0xFF2196F3)
    CustomerStage.QUERIED -> Color(0xFF673AB7)
    CustomerStage.FOLLOW_UP -> Color(0xFFFF9800)
    CustomerStage.WON -> Color(0xFF4CAF50)
}
