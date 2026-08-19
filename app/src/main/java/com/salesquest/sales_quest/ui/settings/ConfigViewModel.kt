package com.salesquest.sales_quest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.services.ConfigImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 配置文件导入/导出 ViewModel */
class ConfigViewModel : ViewModel() {

    val exporting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val importing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val lastImportAt: MutableStateFlow<Long?> = MutableStateFlow(null)

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage

    private val _configJson = MutableStateFlow<String?>(null)
    val configJson: StateFlow<String?> = _configJson

    init {
        viewModelScope.launch {
            val raw = AppContainer.db.settingDao().get("imported_config_at")
            lastImportAt.value = raw?.toLongOrNull()
        }
    }

    fun export() {
        viewModelScope.launch {
            exporting.value = true
            try {
                _configJson.value = AppContainer.configService.exportConfigJson()
                _resultMessage.value = "配置已生成, 请选择保存位置"
            } catch (e: Exception) {
                _resultMessage.value = "导出失败: ${e.message}"
            } finally {
                exporting.value = false
            }
        }
    }

    fun consumeJson() {
        _configJson.value = null
    }

    /** 导入配置文本 */
    fun import(raw: String) {
        viewModelScope.launch {
            importing.value = true
            try {
                when (val result = AppContainer.configService.importConfigJson(raw)) {
                    is ConfigImportResult.Success -> {
                        lastImportAt.value = System.currentTimeMillis()
                        _resultMessage.value = "导入成功 (v${result.version})"
                    }
                    is ConfigImportResult.FormatError -> _resultMessage.value = result.message
                    is ConfigImportResult.VersionError ->
                        _resultMessage.value = "配置版本不兼容: 当前 v${result.found}, 支持 v${result.supported}"
                    is ConfigImportResult.ValidationError -> _resultMessage.value = result.message
                }
            } catch (e: Exception) {
                _resultMessage.value = "导入失败: ${e.message}"
            } finally {
                importing.value = false
            }
        }
    }

    fun clearResult() {
        _resultMessage.value = null
    }
}
