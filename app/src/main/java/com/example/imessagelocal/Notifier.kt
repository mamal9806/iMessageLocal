package com.example.imessagelocal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Notifier {
    fun notifyIncoming(context: Context, title: String, text: String, withSound: Boolean) {
        val channelId = if (withSound) "messages_sound" else "messages_silent"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                if (withSound) "Messages" else "Messages (silent)",
                NotificationManager.IMPORTANCE_HIGH,
            )
            if (!withSound) channel.setSound(null, null)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Notification permission was not granted: stay quiet.
        }
    }
}
