package com.example.imessagelocal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GlassEdge = Color(0xFFD6E9FF)
private val GlassBlue = Color(0xFF4D9DFF)
private val GlassPurple = Color(0xFF9A8BFF)

fun Modifier.liquidGlass(): Modifier {
    val shape = RoundedCornerShape(22.dp)
    return this
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color(0xFF344761).copy(alpha = 0.76f),
                    Color(0xFF17263B).copy(alpha = 0.94f),
                    Color(0xFF25243F).copy(alpha = 0.88f),
                ),
            ),
        )
        .border(
            1.dp,
            Brush.linearGradient(
                listOf(
                    GlassEdge.copy(alpha = 0.76f),
                    GlassBlue.copy(alpha = 0.56f),
                    GlassPurple.copy(alpha = 0.42f),
                    Color.White.copy(alpha = 0.14f),
                ),
            ),
            shape,
        )
}

fun Modifier.liquidGlassCircle(): Modifier {
    return this
        .clip(CircleShape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color(0xFF405A78).copy(alpha = 0.96f),
                    Color(0xFF1B304A).copy(alpha = 0.97f),
                    Color(0xFF302D50).copy(alpha = 0.94f),
                ),
            ),
        )
        .border(
            1.dp,
            Brush.linearGradient(
                listOf(
                    GlassEdge.copy(alpha = 0.92f),
                    GlassBlue.copy(alpha = 0.68f),
                    GlassPurple.copy(alpha = 0.48f),
                ),
            ),
            CircleShape,
        )
}
