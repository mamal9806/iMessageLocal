package com.example.imessagelocal.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontFamily

val IOSBlue = Color(0xFF0A84FF)
val NeonBlue = Color(0xFF1498FF)
val DeepNavy = Color(0xFF050B19)
val PanelNavy = Color(0xFF0C1428)
val PanelNavy2 = Color(0xFF111C35)
val TextPrimary = Color(0xFFF7F9FF)
val TextSecondary = Color(0xFF9AA8C5)
val DividerBlue = Color(0xFF213052)
val SuccessGreen = Color(0xFF28D17C)

@Composable
fun IMessageTheme(darkTheme: Boolean = isSystemInDarkTheme(), fontScale: Float = 1f, content: @Composable () -> Unit) {
    val colors = if (darkTheme) darkColorScheme(
        primary = IOSBlue, onPrimary = Color.White, background = DeepNavy, onBackground = TextPrimary,
        surface = PanelNavy, onSurface = TextPrimary, surfaceVariant = PanelNavy2, onSurfaceVariant = TextSecondary,
        outline = DividerBlue, error = Color(0xFFFF5C65),
    ) else androidx.compose.material3.lightColorScheme(
        primary = IOSBlue, onPrimary = Color.White, background = Color(0xFFF5F7FC), onBackground = Color(0xFF101522),
        surface = Color.White, onSurface = Color(0xFF101522), surfaceVariant = Color(0xFFE9EEF7),
        onSurfaceVariant = Color(0xFF59667D), outline = Color(0xFFD0D7E5), error = Color(0xFFD92D3A),
    )
    val density = LocalDensity.current
    val typography = Typography().copy(
        bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.Default),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = FontFamily.Default),
        titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.Default),
    )
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides Density(density.density, density.fontScale * fontScale),
    ) {
        MaterialTheme(colorScheme = colors, typography = typography, content = content)
    }
}
