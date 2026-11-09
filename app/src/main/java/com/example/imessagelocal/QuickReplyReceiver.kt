package com.example.imessagelocal

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.example.imessagelocal.data.AppDatabase
import com.example.imessagelocal.data.ChatRepository
import com.example.imessagelocal.data.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickReplyReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT)?.toString()?.trim().orEmpty()
        val number = intent.getStringExtra("number").orEmpty()
        val conversationId = intent.getLongExtra("conversation_id", -1L)
        if (text.isBlank() || number.isBlank() || conversationId < 0) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subId = intent.getIntExtra("subscription_id", -1)
                val sms = if (subId >= 0) SmsManager.getSmsManagerForSubscriptionId(subId) else if (android.os.Build.VERSION.SDK_INT >= 31) context.getSystemService(SmsManager::class.java) else @Suppress("DEPRECATION") SmsManager.getDefault()
                val repo = ChatRepository(AppDatabase.get(context).chatDao())
                val messageId = repo.recordOutgoing(conversationId, text, subId.takeIf { it >= 0 })
                val sent = PendingIntent.getBroadcast(context, messageId.toInt(), Intent(context, SmsStatusReceiver::class.java).setAction("com.example.imessagelocal.SMS_SENT").putExtra("message_id", messageId), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                sms.sendTextMessage(number, null, text, sent, null)
                Notifier.notifyReplySent(context, "پاسخ ارسال شد", "در انتظار تأیید شبکه اپراتور")
            } catch (_: Exception) {
                Notifier.notifyReplySent(context, "ارسال ناموفق", "پیامک ارسال نشد؛ مجوز و سیم‌کارت را بررسی کنید")
            } finally { pending.finish() }
        }
    }
    companion object { const val KEY_TEXT = "quick_reply_text" }
}
