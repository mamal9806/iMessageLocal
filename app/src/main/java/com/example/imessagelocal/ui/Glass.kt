package com.example.imessagelocal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.liquidGlass(radius: Int = 22): Modifier {
    val shape = RoundedCornerShape(radius.dp)
    return clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.13f),
                    Color(0xFF3A8DFF).copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.035f),
                ),
            ),
        )
        .border(
            1.dp,
            Brush.linearGradient(
                listOf(Color(0xFF7AB7FF).copy(alpha = 0.55f), Color.White.copy(alpha = 0.09f)),
            ),
            shape,
        )
}

@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF101D3E), DeepNavy, Color.Black),
                    radius = 1200f,
                ),
            ),
    ) { content() }
}
