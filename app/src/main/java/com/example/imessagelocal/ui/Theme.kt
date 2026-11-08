package com.example.imessagelocal.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontFamily

val IOSBlue = Color(0xFF0A84FF)
val NeonBlue = Color(0xFF1498FF)
val DeepNavy = Color(0xFF050B19)
val PanelNavy = Color(0xFF0C1428)
val PanelNavy2 = Color(0xFF111C35)
@get:Composable val TextPrimary: Color get() = MaterialTheme.colorScheme.onSurface
@get:Composable val TextSecondary: Color get() = MaterialTheme.colorScheme.onSurfaceVariant
val DividerBlue = Color(0xFF213052)
val SuccessGreen = Color(0xFF28D17C)

@Composable
fun IMessageTheme(darkTheme: Boolean = isSystemInDarkTheme(), fontScale: Float = 1f, accentTheme: Int = 0, content: @Composable () -> Unit) {
    val accent = when (accentTheme) { 1 -> Color(0xFF8B5CF6); 2 -> Color(0xFF0D9488); 3 -> Color(0xFFE8793A); 4 -> Color(0xFFDB4F83); else -> IOSBlue }
    val colors = (if (darkTheme) darkColorScheme(
        primary = IOSBlue, onPrimary = Color.White,
        background = Color(0xFF05070D), onBackground = Color(0xFFF4F6FC),
        surface = Color(0xFF111827), onSurface = Color(0xFFF4F6FC),
        surfaceVariant = Color(0xFF1B2638), onSurfaceVariant = Color(0xFFB8C3D6),
        outline = Color(0xFF34445E), error = Color(0xFFFF6673),
    ) else androidx.compose.material3.lightColorScheme(
        primary = Color(0xFF0866D8), onPrimary = Color.White,
        background = Color(0xFFF4F6FA), onBackground = Color(0xFF111827),
        surface = Color.White, onSurface = Color(0xFF111827),
        surfaceVariant = Color(0xFFE8EDF5), onSurfaceVariant = Color(0xFF46546A),
        outline = Color(0xFFD2D9E4), error = Color(0xFFBA1A1A),
    )).copy(primary = accent)
    val density = LocalDensity.current
    val baseTypography = Typography()
    val typography = baseTypography.copy(
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = FontFamily.Default),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = FontFamily.Default),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = FontFamily.Default),
    )
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalDensity provides Density(density.density, density.fontScale * fontScale),
    ) {
        MaterialTheme(colorScheme = colors, typography = typography, content = content)
    }
}
