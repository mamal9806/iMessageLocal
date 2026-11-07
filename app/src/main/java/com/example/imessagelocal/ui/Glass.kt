package com.example.imessagelocal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.liquidGlass(): Modifier {
    val shape = RoundedCornerShape(18.dp)
    return this
        .clip(shape)
        .background(
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.20f),
                    Color(0xFF9CCBFF).copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.04f),
                ),
            ),
        )
        .border(
            1.dp,
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.72f), Color(0xFF7EBBFF).copy(alpha = 0.34f), Color.White.copy(alpha = 0.18f)),
            ),
            shape,
        )
}
