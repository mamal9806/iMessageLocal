package com.example.imessagelocal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object Notifier {
    fun notifyIncoming(
        context: Context,
        title: String,
        text: String,
        withSound: Boolean,
        conversationId: Long = -1L,
        soundUri: String? = null,
    ) {
        val channelId = if (withSound) "messages_sound_${(soundUri ?: "default").hashCode()}" else "messages_silent"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    if (withSound) "Messages" else "Messages (silent)",
                    NotificationManager.IMPORTANCE_HIGH,
                )
                if (!withSound) {
                    channel.setSound(null, null)
                } else if (!soundUri.isNullOrBlank()) {
                    channel.setSound(
                        Uri.parse(soundUri),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                }
                manager?.createNotificationChannel(channel)
            }
        }
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversation_id", conversationId)
        }
        val pending = PendingIntent.getActivity(
            context, conversationId.toInt(), open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title).setContentText(text).setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()
        try {
            NotificationManagerCompat.from(context).notify(
                if (conversationId >= 0) conversationId.toInt() else System.currentTimeMillis().toInt(),
                notification,
            )
        } catch (_: SecurityException) { }
    }
}
