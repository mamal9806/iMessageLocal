package com.example.imessagelocal.ui

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTime(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))

fun formatListTime(ms: Long): String =
    if (DateUtils.isToday(ms)) formatTime(ms)
    else SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ms))
