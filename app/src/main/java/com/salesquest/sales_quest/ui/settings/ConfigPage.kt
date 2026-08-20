package com.salesquest.sales_quest.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 配置文件页 - 导入/导出 JSON 配置
 *
 * 导出: 生成 sales_quest_config.json (系统文件选择器选位置)
 * 导入: 选择 JSON → 校验格式/版本/数值 → 写入内部数据库
 * 导入后删除原文件不影响使用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigPage(
    onBack: () -> Unit = {},
    viewModel: ConfigViewModel = viewModel()
) {
    val exporting by viewModel.exporting.collectAsState()
    val importing by viewModel.importing.collectAsState()
    val lastImportAt by viewModel.lastImportAt.collectAsState()
    val configJson by viewModel.configJson.collectAsState()
    val resultMessage by viewModel.resultMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            viewModel.consumeJson()
            return@rememberLauncherForActivityResult
        }
        val json = viewModel.configJson.value
        if (json == null) {
            scope.launch { snackbarHostState.showSnackbar("请稍后重试导出") }
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                snackbarHostState.showSnackbar("配置已导出")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("导出失败: ${e.message}")
            } finally {
                viewModel.consumeJson()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                if (text.isNullOrBlank()) {
                    snackbarHostState.showSnackbar("文件内容为空")
                } else {
                    viewModel.import(text)
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("读取文件失败: ${e.message}")
            }
        }
    }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("配置文件") },
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
            Text(
                "配置文件包含每日任务目标、等级与晋级条件等可配置参数。\n" +
                    "导入后会写入 App 内部存储, 删除原 JSON 文件不影响使用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { viewModel.export() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !exporting
            ) {
                Icon(Icons.Filled.Upload, contentDescription = null)
                
                Text(if (exporting) "正在生成..." else "导出配置")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !importing
            ) {
                Icon(Icons.Filled.Download, contentDescription = null)
                
                Text(if (importing) "正在导入..." else "导入配置")
            }

            if (lastImportAt != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "最近导入: ${formatTime(lastImportAt!!)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 导出 json 就绪后启动系统文件选择器
    // 注意: 不在此处 consumeJson(), 待用户选择保存位置后回调中再消费,
    // 否则文件选择器回调时 configJson 已被清空, 导致导出失败
    LaunchedEffect(configJson) {
        val json = configJson ?: return@LaunchedEffect
        if (json.isNotBlank()) {
            exportLauncher.launch("sales_quest_config.json")
        }
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
