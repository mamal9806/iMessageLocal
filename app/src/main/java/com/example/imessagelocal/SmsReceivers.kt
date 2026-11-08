package com.example.imessagelocal

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.PhoneNumberUtils
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.imessagelocal.data.AppDatabase
import com.example.imessagelocal.data.ChatRepository
import com.example.imessagelocal.data.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun lookupContactName(context: Context, phone: String): String? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) return null
    return try {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(phone).build()
        context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (_: Exception) { null }
}

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
                    val contactName = lookupContactName(context, address)
                    val repo = ChatRepository(AppDatabase.get(context).chatDao())
                    val conversationId = repo.receiveSms(address, body, parts.first().timestampMillis, contactName)
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
                    val settings = Prefs(context).load()
                    if (conversation?.blocked != true && conversation?.spam != true && conversation?.trashed != true) {
                        Notifier.notifyIncoming(context, conversation?.name ?: contactName ?: address, body, settings.sound, conversationId, settings.notificationSoundUri, address)
                    }
                }
            } finally { pending.finish() }
        }
    }
}

class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra("message_id", -1L)
        if (messageId < 0) return
        val state = if (intent.action == "com.example.imessagelocal.SMS_DELIVERED") {
            if (resultCode == Activity.RESULT_OK) "DELIVERED" else "DELIVERY_FAILED"
        } else when (resultCode) {
            Activity.RESULT_OK -> "SENT_TO_NETWORK"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "NO_SERVICE"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "RADIO_OFF"
            SmsManager.RESULT_ERROR_NULL_PDU, SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "FAILED"
            else -> "FAILED"
        }
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { ChatRepository(AppDatabase.get(context).chatDao()).updateDelivery(messageId, state) }
            finally { pending.finish() }
        }
    }
}
