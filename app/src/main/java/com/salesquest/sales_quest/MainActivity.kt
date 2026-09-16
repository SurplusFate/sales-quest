package com.salesquest.sales_quest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.salesquest.sales_quest.core.AppLogger
import com.salesquest.sales_quest.ui.navigation.SalesQuestApp
import com.salesquest.sales_quest.ui.theme.SalesQuestTheme
import com.salesquest.sales_quest.ui.theme.ThemeManager
import com.salesquest.sales_quest.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    // 记录最近一次深浅色判定, 供 onWindowFocusChanged 重试使用(部分 ROM 首次设置会被吞)
    private var lastDark: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLogger.info("MainActivity", "onCreate")
        setContent {
            val mode by ThemeManager.mode.collectAsState()
            val theme by ThemeManager.theme.collectAsState()
            // 与 SalesQuestTheme 保持一致的深浅色判定(只由显示模式决定):
            // 浅色模式 -> 状态栏深色图标; 深色模式 -> 状态栏浅色图标; 跟随系统 -> 随系统
            val isDark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            lastDark = isDark
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    applySystemBarAppearance(isDark)
                }
            }
            SalesQuestTheme(
                mode = mode,
                theme = theme
            ) {
                SalesQuestApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // 窗口获得焦点时系统栏已就绪, 重设图标颜色, 覆盖部分 ROM 上首次设置被吞的场景
        if (hasFocus) {
            window.decorView.post {
                applySystemBarAppearance(lastDark)
                window.decorView.postDelayed({
                    applySystemBarAppearance(lastDark)
                }, 80)
            }
        }
    }

    /**
     * 统一设置状态栏/导航栏图标颜色。
     * 直接使用 Activity 自身 window, 不依赖 view.context(部分系统 DecorView 的 context 是
     * ContextThemeWrapper, 强转 Activity 会崩溃); try-catch 兜底保证系统差异不影响运行。
     */
    private fun applySystemBarAppearance(isDark: Boolean) {
        try {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        } catch (t: Throwable) {
            AppLogger.warning("MainActivity", "applySystemBarAppearance failed: ${t.message}")
        }
    }
}
