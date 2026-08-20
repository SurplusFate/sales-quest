package com.salesquest.sales_quest

import android.app.Application
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.ui.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SalesQuestApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppLogger.info("App", "=== Sales Quest 启动 ===")
        AppContainer.init(this)
        ThemeManager.init(this)
        AppLogger.info("App", "数据库初始化完成")

        // 启动时确保今日任务已创建 (异步执行, 不阻塞 UI)
        appScope.launch {
            try {
                AppContainer.dailyTaskService.ensureTodayTasks()
                AppLogger.info("App", "ensureTodayTasks 完成")
            } catch (e: Exception) {
                AppLogger.error("App", "ensureTodayTasks 失败: $e", e.stackTraceToString())
            }
        }

        // 自动备份 (WebDAV 开启且距上次超 24h 时)
        appScope.launch {
            try {
                AppContainer.webDavService.maybeAutoBackup()
            } catch (e: Exception) {
                AppLogger.error("App", "自动备份失败: $e", e.stackTraceToString())
            }
        }
    }
}
