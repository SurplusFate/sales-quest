package com.salesquest.sales_quest.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 坚果云 WebDAV 云备份页
 * 账号配置 / 测试连接 / 立即备份 / 查看备份 / 恢复备份 / 自动备份
 * 密码通过 EncryptedSharedPreferences 加密存储
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebDavPage(
    onBack: () -> Unit = {},
    viewModel: WebDavViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmRestore by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("坚果云 WebDAV") },
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
                "备份内容: 数据库、每日/历史数据、客户、等级进度、总结等。\n\n" +
                    "坚果云配置步骤:\n" +
                    "1. 登录 jianguoyun.com → 设置 → 安全选项\n" +
                    "2. 「第三方应用管理」→ 添加应用, 名称任意\n" +
                    "3. 复制生成的「应用密码」填入下方密码栏\n" +
                    "4. 在坚果云根目录手动创建备份目录 (如 SalesQuest)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.url,
                onValueChange = viewModel::onUrlChange,
                label = { Text("WebDAV 地址") },
                placeholder = { Text("https://dav.jianguoyun.com/dav") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("密码 / 应用密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (state.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (state.passwordVisible) "隐藏密码" else "显示密码"
                        )
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.dir,
                onValueChange = viewModel::onDirChange,
                label = { Text("备份目录") },
                placeholder = { Text("/SalesQuest") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("自动备份", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "每天自动备份一次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.autoBackup, onCheckedChange = viewModel::onAutoBackupChange)
            }

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = viewModel::saveConfig,
                    modifier = Modifier.weight(1f),
                    enabled = !state.busy
                ) { Text("保存配置") }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = viewModel::testConnection,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.busy
            ) { Text("测试连接") }

            Spacer(Modifier.height(16.dp))
            Text("云备份", style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (state.busy) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.height(24.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("处理中...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = viewModel::backupNow,
                    modifier = Modifier.weight(1f),
                    enabled = !state.busy && state.isConfigured
                ) { Text("立即备份") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = viewModel::refreshBackups,
                    modifier = Modifier.weight(1f),
                    enabled = !state.busy && state.isConfigured
                ) { Text("获取备份") }
            }

            if (state.lastBackupAt != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "上次备份: ${formatTime(state.lastBackupAt!!)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.backups.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("已有备份", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                state.backups.forEach { backup ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(backup.name, modifier = Modifier.weight(1f))
                        TextButton(onClick = { confirmRestore = backup.name }) {
                            Text("恢复")
                        }
                    }
                }
            }
        }
    }

    confirmRestore?.let { filename ->
        AlertDialog(
            onDismissRequest = { confirmRestore = null },
            title = { Text("恢复备份") },
            text = { Text("恢复备份会覆盖当前数据, 请确认已经备份当前数据。\n\n确定恢复 $filename 吗?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = null
                    viewModel.restore(filename)
                }) { Text("覆盖恢复") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = null }) { Text("取消") }
            }
        )
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
