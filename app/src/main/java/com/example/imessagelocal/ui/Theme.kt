package com.example.imessagelocal.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val IOSBlue = Color(0xFF69AFFF)
val IOSBackground = Color(0xFF080D18)
val IOSSearchFill = Color(0xFF172235)
val GlassSurface = Color(0xFF111C2D)
val GlassElevated = Color(0xFF1A2A40)
val AccentViolet = Color(0xFF9A8BFF)
val AccentMint = Color(0xFF62DCC8)

@Composable
fun IMessageTheme(darkTheme: Boolean, fontScale: Float, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = IOSBlue,
            onPrimary = Color(0xFF071426),
            secondary = AccentViolet,
            onSecondary = Color(0xFF17132D),
            tertiary = AccentMint,
            background = IOSBackground,
            onBackground = Color(0xFFF3F6FC),
            surface = GlassSurface,
            onSurface = Color(0xFFF3F6FC),
            surfaceVariant = GlassElevated,
            onSurfaceVariant = Color(0xFFABB9CC),
            outline = Color(0xFF40536D),
            error = Color(0xFFFF7185),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF176FE5),
            onPrimary = Color.White,
            secondary = Color(0xFF6750A4),
            background = Color(0xFFF3F6FB),
            onBackground = Color(0xFF111827),
            surface = Color.White,
            onSurface = Color(0xFF111827),
            surfaceVariant = Color(0xFFE8EEF7),
            onSurfaceVariant = Color(0xFF596579),
            outline = Color(0xFFD2DCEB),
            error = Color(0xFFBA1A1A),
        )
    }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * fontScale),
    ) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
