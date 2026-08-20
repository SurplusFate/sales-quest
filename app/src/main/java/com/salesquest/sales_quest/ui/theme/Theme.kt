package com.salesquest.sales_quest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    surface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    error = Color(0xFFEF4444),
    outline = Color(0xFF334155)
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
    surface = Color(0xFF0A0A0F),
    surfaceVariant = Color(0xFF1A1A2E),
    onSurfaceVariant = Color(0xFF9E9E9E),
    background = Color(0xFF0A0A0F),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFFF5252),
    outline = Color(0xFF333333)
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
    surface = Color(0xFF0D1F1C),
    surfaceVariant = Color(0xFF1B3530),
    onSurfaceVariant = Color(0xFF80CBC4),
    background = Color(0xFF0D1F1C),
    onBackground = Color(0xFFE0F2F1),
    onSurface = Color(0xFFE0F2F1),
    error = Color(0xFFEF5350),
    outline = Color(0xFF37474F)
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
    val isDark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme() || theme == AppTheme.DARK_NEON
        ThemeMode.LIGHT -> theme == AppTheme.DARK_NEON
        ThemeMode.DARK -> true
    }

    val colorScheme = when (theme) {
        AppTheme.BLUE_AMBER -> if (isDark) BlueAmberDark else BlueAmberLight
        AppTheme.DARK_NEON -> if (isDark) DarkNeonDark else DarkNeonLight
        AppTheme.TEAL_CORAL -> if (isDark) TealCoralDark else TealCoralLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content
    )
}
