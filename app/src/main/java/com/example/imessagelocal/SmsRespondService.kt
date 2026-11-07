package com.example.imessagelocal

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SmsRespondService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.data?.schemeSpecificPart.orEmpty()
        startActivity(Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_SENDTO
            data = android.net.Uri.fromParts("smsto", number, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
