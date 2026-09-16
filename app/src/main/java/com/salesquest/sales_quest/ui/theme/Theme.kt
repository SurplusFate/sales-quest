package com.salesquest.sales_quest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/**
 * 全 app 共享的深浅色判定唯一来源。
 * 由 SalesQuestTheme 依据显示模式计算并注入, 所有组件(状态栏/玻璃/卡片)统一读取,
 * 避免各处自行用色板亮度或配色方案推断导致判定漂移。
 */
val LocalIsDark = staticCompositionLocalOf { false }

/**
 * 深色模式下将固定的强调色提亮到 Material 200 档, 避免彩色数值/图标
 * 在深色玻璃卡片上对比度不足 (典型: #9C27B0 紫、#F44336 红在深底上偏暗)。
 * 浅色模式原样返回, 不影响现有浅色观感。
 */
/**
 * 全局对话框/底部弹窗遮罩(scrim)颜色。
 * M3 深色模式默认黑色遮罩高达 60%, 会把深色页面压成几乎全黑(用户反复反馈"黑不溜秋")。
 * 这里深色用 25% 黑、浅色用 30% 黑, 保留模态聚焦感, 同时背后页面仍然可读。
 */
@Composable
fun appScrim(): Color = if (LocalIsDark.current) Color(0x40000000) else Color(0x4D000000)

/**
 * 放进 AlertDialog / DatePickerDialog 的内容区, 把平台窗口的黑色遮罩(dim)从
 * 深色默认 ~0.6 调低到 0.25(浅色保持 0.32), 解决"弹窗背后整页被压成黑乎乎"。
 */
@Composable
fun DialogScrimAdjuster() {
    val isDark = LocalIsDark.current
    val view = LocalView.current
    val provider = remember { view.parent as? DialogWindowProvider }
    LaunchedEffect(provider, isDark) {
        provider?.window?.setDimAmount(if (isDark) 0.25f else 0.32f)
    }
}

/**
 * 深色模式下将固定的强调色提亮到 Material 200 档, 避免彩色数值/图标
 * 在深色玻璃卡片上对比度不足 (典型: #9C27B0 紫、#F44336 红在深底上偏暗)。
 * 浅色模式原样返回, 不影响现有浅色观感。
 */
@Composable
fun accentForDark(lightColor: Color): Color {
    if (!LocalIsDark.current) return lightColor
    return when (lightColor) {
        Color(0xFF2196F3) -> Color(0xFF64B5F6)
        Color(0xFF9C27B0) -> Color(0xFFCE93D8)
        Color(0xFFF44336) -> Color(0xFFEF9A9A)
        Color(0xFF4CAF50) -> Color(0xFF81C784)
        Color(0xFFFF9800) -> Color(0xFFFFB74D)
        else -> lightColor
    }
}

// ==================== 深蓝 + 琥珀 ====================
private val BlueAmberLight = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFFFFB300),
    onSecondary = Color(0xFF3E2723),
    tertiary = Color(0xFF6C5CE7),
    surface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B),
    error = Color(0xFFEF4444),
    outline = Color(0xFFCBD5E1)
)

private val BlueAmberDark = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFFFFD54F),
    onSecondary = Color(0xFF3E2723),
    tertiary = Color(0xFF9FA8DA),
    surface = Color(0xFF243252),
    surfaceVariant = Color(0xFF2B3B5E),
    onSurfaceVariant = Color(0xFFDDE4EF),
    background = Color(0xFF243252),
    onBackground = Color(0xFFEEF2F9),
    onSurface = Color(0xFFEEF2F9),
    error = Color(0xFFEF4444),
    outline = Color(0xFF9AA8BE),
    surfaceContainerLowest = Color(0xFF1B2745),
    surfaceContainerLow = Color(0xFF293657),
    surfaceContainer = Color(0xFF2E3C60),
    surfaceContainerHigh = Color(0xFF35446E),
    surfaceContainerHighest = Color(0xFF3E4D79),
    outlineVariant = Color(0xFF3C4A6B)
)

// ==================== 暗黑 + 霓虹 ====================
private val DarkNeonLight = lightColorScheme(
    primary = Color(0xFF6C5CE7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    onPrimaryContainer = Color(0xFF311B92),
    secondary = Color(0xFF00C853),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF6B35),
    surface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF616161),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121),
    error = Color(0xFFFF5252),
    outline = Color(0xFFBDBDBD)
)

private val DarkNeonDark = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFF6C5CE7),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF6B35),
    surface = Color(0xFF1E1E2C),
    surfaceVariant = Color(0xFF272737),
    onSurfaceVariant = Color(0xFFE0E0EA),
    background = Color(0xFF1E1E2C),
    onBackground = Color(0xFFF0F0F8),
    onSurface = Color(0xFFF0F0F8),
    error = Color(0xFFFF5252),
    outline = Color(0xFF8A8AA2),
    surfaceContainerLowest = Color(0xFF15151E),
    surfaceContainerLow = Color(0xFF242433),
    surfaceContainer = Color(0xFF2B2B3C),
    surfaceContainerHigh = Color(0xFF343447),
    surfaceContainerHighest = Color(0xFF3D3D55),
    outlineVariant = Color(0xFF3C3C55)
)

// ==================== 青绿 + 珊瑚 ====================
private val TealCoralLight = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = Color(0xFFFF7043),
    onSecondary = Color.White,
    tertiary = Color(0xFF5C6BC0),
    surface = Color(0xFFFAFAF5),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF455A64),
    background = Color(0xFFFAFAF5),
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20),
    error = Color(0xFFE53935),
    outline = Color(0xFFB0BEC5)
)

private val TealCoralDark = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF003C32),
    primaryContainer = Color(0xFF00695C),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFFFFAB91),
    onSecondary = Color(0xFF3E2723),
    tertiary = Color(0xFF9FA8DA),
    surface = Color(0xFF223D34),
    surfaceVariant = Color(0xFF2A4D44),
    onSurfaceVariant = Color(0xFFCDF5ED),
    background = Color(0xFF223D34),
    onBackground = Color(0xFFEFFBF8),
    onSurface = Color(0xFFEFFBF8),
    error = Color(0xFFEF5350),
    outline = Color(0xFF7CA097),
    surfaceContainerLowest = Color(0xFF193129),
    surfaceContainerLow = Color(0xFF27483E),
    surfaceContainer = Color(0xFF2D5045),
    surfaceContainerHigh = Color(0xFF34584C),
    surfaceContainerHighest = Color(0xFF3C6357),
    outlineVariant = Color(0xFF3C6357)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun SalesQuestTheme(
    mode: ThemeMode = ThemeManager.mode.value,
    theme: AppTheme = ThemeManager.theme.value,
    content: @Composable () -> Unit
) {
    // 深浅色只由显示模式决定: LIGHT 恒浅色 / DARK 恒深色 / SYSTEM 跟随系统。
    // 配色方案(theme)只决定色板, 不再强制深色, 否则"浅色模式+DARK_NEON"会错误地渲染成深色。
    val isDark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when (theme) {
        AppTheme.BLUE_AMBER -> if (isDark) BlueAmberDark else BlueAmberLight
        AppTheme.DARK_NEON -> if (isDark) DarkNeonDark else DarkNeonLight
        AppTheme.TEAL_CORAL -> if (isDark) TealCoralDark else TealCoralLight
    }

    // 根因修复(1.0.37): 页面容器(GlassRoot/HomePage)全用 Box+background(), 从不提供
    // LocalContentColor; 未显式写 color 的 Text 会一路回退到 LocalContentColor 的
    // 默认值 Color.Black, 导致深色模式下"销售新人/今日任务"等标题黑字配深底。
    // 这里在主题层补上默认 contentColor=onSurface, Surface 等组件内部仍会按自身
    // contentColor 局部覆盖, 行为不受影响。
    CompositionLocalProvider(
        LocalIsDark provides isDark,
        LocalContentColor provides colorScheme.onSurface
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = AppShapes,
            content = content
        )
    }
}
