package com.salesquest.sales_quest.ui.customers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SaveCustomerParams
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.Operator
import kotlinx.coroutines.launch

/** 新增/编辑客户页 - 所有字段可选 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormPage(
    customerId: String?,
    onBack: () -> Unit = {},
    viewModel: CustomerFormViewModel = viewModel(factory = CustomerFormViewModel.factory(customerId))
) {
    val customer by viewModel.customer.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isEdit = customerId != null

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var actualCost by remember { mutableStateOf("") }
    var traffic by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var subCards by remember { mutableStateOf("") }
    var operator by remember { mutableStateOf(Operator.UNKNOWN) }
    var selfReportedCost by remember { mutableStateOf<Int?>(null) }
    var stage by remember { mutableStateOf(CustomerStage.NEW) }
    var broadband by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(customer?.id) {
        val c = customer
        if (c != null) {
            name = c.name
            phone = c.phone
            note = c.note ?: ""
            packageName = c.packageName ?: ""
            actualCost = c.actualCost?.toString() ?: ""
            traffic = c.traffic ?: ""
            minutes = c.minutes ?: ""
            subCards = if (c.subCards > 0) c.subCards.toString() else ""
            operator = Operator.fromCode(c.operator)
            selfReportedCost = c.selfReportedCost
            stage = CustomerStage.fromCode(c.salesStage)
            broadband = c.broadband
            camera = c.camera
        }
    }

    fun save() {
        if (saving) return
        saving = true
        scope.launch {
            try {
                val params = SaveCustomerParams(
                    id = customerId,
                    name = name.trim().ifEmpty { null },
                    phone = phone.trim().ifEmpty { null },
                    operator = operator,
                    selfReportedCost = selfReportedCost,
                    actualCost = actualCost.trim().toIntOrNull(),
                    packageName = packageName.trim().ifEmpty { null },
                    traffic = traffic.trim().ifEmpty { null },
                    minutes = minutes.trim().ifEmpty { null },
                    broadband = broadband,
                    subCards = subCards.trim().toIntOrNull() ?: 0,
                    camera = camera,
                    stage = stage,
                    note = note.trim().ifEmpty { null }
                )
                val savedId = AppContainer.saveCustomer(params)
                snackbarHostState.showSnackbar("保存成功")
                if (isEdit && customerId != null) {
                    onBack()
                } else {
                    onBack()
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("保存失败: ${e.message}")
                saving = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isEdit) "编辑客户" else "新增客户") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = ::save, enabled = !saving) {
                        if (saving) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Text("保存")
                        }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // === 基础信息 ===
            SectionTitle("基础信息")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("客户称呼") },
                placeholder = { Text("如: 张哥, 不填自动编号") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("手机号") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            ChipGroup(
                label = "运营商",
                options = Operator.entries.map { it.label to it },
                selected = operator,
                onSelected = { operator = it }
            )
            Spacer(Modifier.height(12.dp))
            SelfCostChips(
                selected = selfReportedCost,
                onSelected = { selfReportedCost = it }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注") },
                placeholder = { Text("记录关键信息...") },
                leadingIcon = { Icon(Icons.Filled.Note, contentDescription = null) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            // === 套餐详情 (仅编辑时可填) ===
            if (isEdit) {
                Spacer(Modifier.height(24.dp))
                SectionTitle("套餐详情")
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("套餐名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = actualCost,
                    onValueChange = { actualCost = it },
                    label = { Text("实际月消费 (元)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = traffic,
                    onValueChange = { traffic = it },
                    label = { Text("流量") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it },
                    label = { Text("通话分钟") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                SwitchRow("有宽带", broadband) { broadband = it }
                SwitchRow("有摄像头", camera) { camera = it }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = subCards,
                    onValueChange = { subCards = it },
                    label = { Text("副卡数量") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // === 客户状态 ===
            Spacer(Modifier.height(24.dp))
            SectionTitle("客户状态")
            ChipGroup(
                label = "当前状态",
                options = CustomerStage.entries.map { it.label to it },
                selected = stage,
                onSelected = { stage = it }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun <T> ChipGroup(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (optionLabel, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                    label = { Text(optionLabel) }
                )
            }
        }
    }
}

/** 月消费自报 ChoiceChip: 不清楚 / 60+ / 100+ / 150+ / 200+ / 300+ */
@Composable
fun SelfCostChips(
    selected: Int?,
    onSelected: (Int?) -> Unit
) {
    val options = listOf<Pair<String, Int?>>(
        "不清楚" to null,
        "60+" to 60,
        "100+" to 100,
        "150+" to 150,
        "200+" to 200,
        "300+" to 300
    )
    Column {
        Text("月消费自报", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (optionLabel, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                    label = { Text(optionLabel) }
                )
            }
        }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = { Text(label) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}
