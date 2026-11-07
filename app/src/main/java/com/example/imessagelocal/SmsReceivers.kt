package com.example.imessagelocal

import android.app.Activity
import android.content.ContentValues
import android.provider.Telephony
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.example.imessagelocal.data.AppDatabase
import com.example.imessagelocal.data.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION &&
            intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (parts.isEmpty()) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = parts.first().originatingAddress.orEmpty()
                val body = parts.joinToString(separator = "") { it.messageBody.orEmpty() }
                if (address.isNotBlank() && body.isNotBlank()) {
                    val repo = ChatRepository(AppDatabase.get(context).chatDao())
                    val conversationId = repo.receiveSms(address, body, parts.first().timestampMillis)
                    if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
                        val values = ContentValues().apply {
                            put(Telephony.Sms.ADDRESS, address)
                            put(Telephony.Sms.BODY, body)
                            put(Telephony.Sms.DATE, parts.first().timestampMillis)
                            put(Telephony.Sms.READ, 0)
                            put(Telephony.Sms.SEEN, 0)
                            put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                        }
                        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                    }
                    val conversation = repo.getConversation(conversationId)
                    Notifier.notifyIncoming(context, conversation?.name ?: address, body, true, conversationId)
                }
            } finally { pending.finish() }
        }
    }
}

class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra("message_id", -1L)
        if (messageId < 0) return
        val state = when (resultCode) {
            Activity.RESULT_OK -> "SENT_TO_NETWORK"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "NO_SERVICE"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "RADIO_OFF"
            SmsManager.RESULT_ERROR_NULL_PDU -> "FAILED"
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "FAILED"
            else -> "FAILED"
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { ChatRepository(AppDatabase.get(context).chatDao()).updateDelivery(messageId, state) }
            finally { pending.finish() }
        }
    }
}
