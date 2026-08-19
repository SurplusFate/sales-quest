package com.salesquest.sales_quest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.services.BackupFileInfo
import com.salesquest.sales_quest.services.WebDavConfig
import com.salesquest.sales_quest.services.WebDavResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 云备份页状态 */
data class WebDavUiState(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val dir: String = "",
    val autoBackup: Boolean = false,
    val isConfigured: Boolean = false,
    val busy: Boolean = false,
    val backups: List<BackupFileInfo> = emptyList(),
    val lastBackupAt: Long? = null,
    val message: String? = null,
    val passwordVisible: Boolean = false
)

/** 坚果云 WebDAV ViewModel */
class WebDavViewModel : ViewModel() {

    private val service get() = AppContainer.webDavService
    private val store get() = AppContainer.webDavConfigStore

    private val _uiState = MutableStateFlow(WebDavUiState())
    val uiState: StateFlow<WebDavUiState> = _uiState

    init {
        loadConfig()
    }

    fun loadConfig() {
        val c = store.load()
        _uiState.value = _uiState.value.copy(
            url = c.url,
            username = c.username,
            password = c.password,
            dir = c.dir,
            autoBackup = c.autoBackup,
            isConfigured = store.isConfigured(),
            lastBackupAt = store.lastBackupAt().takeIf { it > 0 }
        )
    }

    fun onUrlChange(v: String) = _uiState.update { it.copy(url = v) }
    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v) }
    fun onDirChange(v: String) = _uiState.update { it.copy(dir = v) }
    fun onAutoBackupChange(v: Boolean) = _uiState.update { it.copy(autoBackup = v) }
    fun togglePasswordVisibility() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    private fun MutableStateFlow<WebDavUiState>.update(transform: (WebDavUiState) -> WebDavUiState) {
        value = transform(value)
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun currentConfig(): WebDavConfig {
        val s = _uiState.value
        return WebDavConfig(
            url = s.url,
            username = s.username,
            password = s.password,
            dir = s.dir,
            autoBackup = s.autoBackup
        )
    }

    fun saveConfig() {
        val config = currentConfig()
        if (config.url.isBlank()) return showMessage("请填写 WebDAV 地址")
        if (config.username.isBlank()) return showMessage("请填写用户名")
        if (config.password.isBlank()) return showMessage("请填写密码/应用密码")
        store.save(config)
        _uiState.update {
            it.copy(isConfigured = true, message = "配置已保存")
        }
    }

    fun testConnection() {
        val config = currentConfig()
        if (config.url.isBlank()) return showMessage("请填写 WebDAV 地址")
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = service.testConnection(config)
            _uiState.update { it.copy(busy = false, message = (result as? WebDavResult.Success)?.message
                ?: (result as? WebDavResult.Failure)?.message) }
        }
    }

    fun backupNow() {
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = service.backupNow()
            _uiState.update {
                it.copy(
                    busy = false,
                    message = (result as? WebDavResult.Success)?.message
                        ?: (result as? WebDavResult.Failure)?.message,
                    lastBackupAt = store.lastBackupAt().takeIf { time -> time > 0 }
                )
            }
        }
    }

    fun refreshBackups() {
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = service.listBackups()
            _uiState.update { state ->
                val backups = if (result is WebDavResult.Success) {
                    result.message.split("\n").filter { it.isNotBlank() }.map { BackupFileInfo(it, 0) }
                } else emptyList()
                state.copy(
                    busy = false,
                    backups = backups,
                    message = (result as? WebDavResult.Failure)?.message
                )
            }
        }
    }

    fun restore(filename: String) {
        _uiState.update { it.copy(busy = true) }
        viewModelScope.launch {
            val result = service.restoreBackup(filename)
            _uiState.update {
                it.copy(
                    busy = false,
                    message = (result as? WebDavResult.Success)?.message
                        ?: (result as? WebDavResult.Failure)?.message
                )
            }
        }
    }

    private fun showMessage(msg: String) {
        _uiState.update { it.copy(message = msg) }
    }
}
