package com.salesquest.sales_quest.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 主题模式 */
enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("夜间模式")
}

/** 主题配色方案 */
enum class AppTheme(val label: String, val desc: String) {
    BLUE_AMBER("深蓝 + 琥珀", "专业 · 能量 · 信任"),
    DARK_NEON("暗黑 + 霓虹", "游戏感 · 高对比 · 沉浸"),
    TEAL_CORAL("青绿 + 珊瑚", "清新 · 活力 · 现代感")
}

/** 主题持久化管理 */
object ThemeManager {

    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_MODE = "theme_mode"
    private const val KEY_THEME = "app_theme"

    private val _mode = MutableStateFlow(ThemeMode.SYSTEM)
    val mode: StateFlow<ThemeMode> = _mode

    private val _theme = MutableStateFlow(AppTheme.BLUE_AMBER)
    val theme: StateFlow<AppTheme> = _theme

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeOrdinal = prefs.getInt(KEY_MODE, ThemeMode.SYSTEM.ordinal)
        val themeOrdinal = prefs.getInt(KEY_THEME, AppTheme.BLUE_AMBER.ordinal)
        _mode.value = ThemeMode.entries.getOrElse(modeOrdinal) { ThemeMode.SYSTEM }
        _theme.value = AppTheme.entries.getOrElse(themeOrdinal) { AppTheme.BLUE_AMBER }
        initialized = true
    }

    fun setMode(context: Context, mode: ThemeMode) {
        _mode.value = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MODE, mode.ordinal).apply()
    }

    fun setTheme(context: Context, theme: AppTheme) {
        _theme.value = theme
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_THEME, theme.ordinal).apply()
    }
}
