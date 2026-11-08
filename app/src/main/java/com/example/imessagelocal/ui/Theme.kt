package com.example.imessagelocal.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// Accent is read from the active Material theme so every screen follows the selected palette.
@get:Composable val IOSBlue: Color get() = MaterialTheme.colorScheme.primary
val NeonBlue = Color(0xFF1498FF)
val DeepNavy = Color(0xFF05070D)
val PanelNavy = Color(0xFF11151D)
val PanelNavy2 = Color(0xFF202631)
@get:Composable val TextPrimary: Color get() = MaterialTheme.colorScheme.onSurface
@get:Composable val TextSecondary: Color get() = MaterialTheme.colorScheme.onSurfaceVariant
val DividerBlue = Color(0xFF344052)
val SuccessGreen = Color(0xFF28D17C)

private data class AccentPalette(
    val primary: Color,
    val darkPrimary: Color,
    val container: Color,
    val darkContainer: Color,
)

private fun palette(index: Int) = when (index) {
    1 -> AccentPalette(Color(0xFF7C3AED), Color(0xFFB69CFF), Color(0xFFEDE5FF), Color(0xFF39265F))
    2 -> AccentPalette(Color(0xFF0F9488), Color(0xFF55D6C8), Color(0xFFD8F5F1), Color(0xFF164B48))
    3 -> AccentPalette(Color(0xFFB96A12), Color(0xFFFFBE69), Color(0xFFFFEBD2), Color(0xFF593817))
    4 -> AccentPalette(Color(0xFFC13F78), Color(0xFFFF91BC), Color(0xFFFFE1ED), Color(0xFF5B2941))
    else -> AccentPalette(Color(0xFF1769D2), Color(0xFF72B7FF), Color(0xFFDCEBFF), Color(0xFF1E416B))
}

@Composable
fun IMessageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    accentTheme: Int = 0,
    content: @Composable () -> Unit,
) {
    val accent = palette(accentTheme)
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = accent.darkPrimary,
            onPrimary = Color(0xFF07111D),
            primaryContainer = accent.darkContainer,
            onPrimaryContainer = Color(0xFFF7F8FC),
            secondary = accent.darkPrimary,
            onSecondary = Color(0xFF07111D),
            tertiary = accent.darkPrimary,
            background = Color(0xFF07090D),
            onBackground = Color(0xFFF7F8FC),
            surface = Color(0xFF11151D),
            onSurface = Color(0xFFF7F8FC),
            surfaceVariant = Color(0xFF202631),
            onSurfaceVariant = Color(0xFFC2CAD6),
            outline = Color(0xFF3B4656),
            error = Color(0xFFFF7885),
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            onPrimary = Color.White,
            primaryContainer = accent.container,
            onPrimaryContainer = Color(0xFF171923),
            secondary = accent.primary,
            onSecondary = Color.White,
            tertiary = accent.primary,
            background = Color(0xFFF4F6FA),
            onBackground = Color(0xFF171923),
            surface = Color.White,
            onSurface = Color(0xFF171923),
            surfaceVariant = Color(0xFFE9EDF4),
            onSurfaceVariant = Color(0xFF4D596B),
            outline = Color(0xFFD0D7E2),
            error = Color(0xFFBA1A1A),
        )
    }
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
