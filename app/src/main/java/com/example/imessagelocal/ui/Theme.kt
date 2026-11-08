package com.example.imessagelocal.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

val IOSBlue = Color(0xFF0A84FF)
val IOSBackground = Color(0xFFFFFFFF)
val IOSSearchFill = Color(0xFFF2F2F7)

@Composable
fun IMessageTheme(darkTheme: Boolean, fontScale: Float, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = IOSBlue,
            onPrimary = Color.White,
            background = Color.Black,
            onBackground = Color(0xFFF2F2F7),
            surface = Color(0xFF1C1C1E),
            onSurface = Color(0xFFF2F2F7),
            surfaceVariant = Color(0xFF2C2C2E),
            onSurfaceVariant = Color(0xFF8E8E93),
            outline = Color(0xFF38383A),
            error = Color(0xFFFF453A),
        )
    } else {
        lightColorScheme(
            primary = IOSBlue,
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFE9E9EB),
            onSurfaceVariant = Color(0xFF8E8E93),
            outline = Color(0xFFC6C6C8),
            error = Color(0xFFFF3B30),
        )
    }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * fontScale),
    ) {
        MaterialTheme(colorScheme = colors, content = content)
    }
}
