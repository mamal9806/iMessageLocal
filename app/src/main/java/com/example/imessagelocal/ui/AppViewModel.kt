package com.example.imessagelocal.ui

import android.app.Application
import android.telephony.SmsManager
import android.provider.Telephony
import android.provider.ContactsContract
import android.net.Uri
import android.os.Build
import android.app.PendingIntent
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.imessagelocal.Notifier
import com.example.imessagelocal.data.AppDatabase
import com.example.imessagelocal.data.ChatRepository
import com.example.imessagelocal.data.ConversationEntity
import com.example.imessagelocal.data.MessageEntity
import com.example.imessagelocal.data.Prefs
import com.example.imessagelocal.data.Settings
import com.example.imessagelocal.data.ScheduledSmsEntity
import android.app.AlarmManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChatRepository(AppDatabase.get(app).chatDao())
    private val prefs = Prefs(app)

    var smsStatus by mutableStateOf("")
        private set

    var settings by mutableStateOf(prefs.load())
        private set

    private val query = MutableStateFlow("")
    private val openId = MutableStateFlow<Long?>(null)
    val openConversationId: StateFlow<Long?> = openId.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = query
        .flatMapLatest { repo.conversations(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val everything: StateFlow<List<ConversationEntity>> = repo.conversations("")
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val messages: StateFlow<List<MessageEntity>> = openId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.messages(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val scheduledMessages: StateFlow<List<ScheduledSmsEntity>> = openId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repo.scheduledMessages(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateSettings(s: Settings) {
        settings = s
        prefs.save(s)
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun open(id: Long?) {
        openId.value = id
    }

    fun conversationName(id: Long): String =
        everything.value.firstOrNull { it.id == id }?.name.orEmpty()

    fun conversationNumber(id: Long): String =
        everything.value.firstOrNull { it.id == id }?.phoneNumber.orEmpty()

    fun createConversation(name: String, phoneNumber: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { openId.value = repo.createConversation(clean, phoneNumber.trim()) }
    }

    fun send(conversationId: Long, text: String, replyTo: MessageEntity?) {
        viewModelScope.launch { repo.addMessage(conversationId, text, isOutgoing = true, replyTo = replyTo) }
    }

    fun sendSms(conversationId: Long, text: String, replyTo: MessageEntity?) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            var recordedMessageId = -1L
            try {
                val conversation = repo.getConversation(conversationId)
                val number = conversation?.phoneNumber.orEmpty()
                if (number.isBlank()) { smsStatus = "برای این گفتگو شماره تلفن ثبت نشده است."; return@launch }
                val chosenSubId = conversation?.simSubscriptionId?.takeIf { it >= 0 }
                    ?: settings.defaultSimSubscriptionId.takeIf { it >= 0 }
                val subId = chosenSubId
                val manager = if (subId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getApplication<Application>().getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION") SmsManager.getDefault()
                }
                val messageId = repo.recordOutgoing(conversationId, clean, subId)
                recordedMessageId = messageId
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                fun callback(action: String, requestCode: Int) = PendingIntent.getBroadcast(
                    getApplication(), requestCode,
                    Intent(getApplication(), com.example.imessagelocal.SmsStatusReceiver::class.java)
                        .setAction(action).putExtra("message_id", messageId), flags,
                )
                val parts = manager.divideMessage(clean)
                if (parts.size > 1) {
                    val sentCallbacks = ArrayList<PendingIntent>()
                    val deliveryCallbacks = ArrayList<PendingIntent>()
                    parts.indices.forEach { index ->
                        sentCallbacks.add(callback("com.example.imessagelocal.SMS_SENT", messageId.toInt() + index))
                        deliveryCallbacks.add(callback("com.example.imessagelocal.SMS_DELIVERED", messageId.toInt() + 100000 + index))
                    }
                    manager.sendMultipartTextMessage(number, null, parts, sentCallbacks, deliveryCallbacks)
                } else manager.sendTextMessage(
                    number, null, clean,
                    callback("com.example.imessagelocal.SMS_SENT", messageId.toInt()),
                    callback("com.example.imessagelocal.SMS_DELIVERED", messageId.toInt() + 100000),
                )
                smsStatus = "در انتظار نتیجه شبکه اپراتور…"
            } catch (e: SecurityException) {
                if (recordedMessageId >= 0L) repo.updateDelivery(recordedMessageId, "FAILED")
                smsStatus = "مجوز ارسال پیامک داده نشده است."
            } catch (e: Exception) {
                if (recordedMessageId >= 0L) repo.updateDelivery(recordedMessageId, "FAILED")
                smsStatus = "ارسال ناموفق: ${e.message ?: "خطای نامشخص"}"
            }
        }
    }

    private fun findSavedContactName(address: String): String? {
        if (address.isBlank()) return null
        return try {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
            getApplication<Application>().contentResolver.query(
                lookupUri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null, null, null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0)?.takeIf { it.isNotBlank() } else null
            }
        } catch (_: Exception) { null }
    }

    fun restorePreviousMessages() {
        val context = getApplication<Application>()
        if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
            smsStatus = "برای بازیابی پیام‌های قبلی، ابتدا A SMS را برنامه پیش‌فرض پیامک کنید."
            return
        }
        viewModelScope.launch {
            try {
                var imported = 0
                val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE)
                context.contentResolver.query(Telephony.Sms.CONTENT_URI, projection, null, null, Telephony.Sms.DATE + " ASC")?.use { cursor ->
                    val addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                    val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                    val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                    val typeCol = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
                    while (cursor.moveToNext()) {
                        val type = cursor.getInt(typeCol)
                        if (type == Telephony.Sms.MESSAGE_TYPE_INBOX || type == Telephony.Sms.MESSAGE_TYPE_SENT ||
                            type == Telephony.Sms.MESSAGE_TYPE_OUTBOX || type == Telephony.Sms.MESSAGE_TYPE_FAILED ||
                            type == Telephony.Sms.MESSAGE_TYPE_QUEUED) {
                            val address = cursor.getString(addressCol).orEmpty()
                            val contactName = findSavedContactName(address)
                            repo.importSms(address, cursor.getString(bodyCol).orEmpty(),
                                cursor.getLong(dateCol), type != Telephony.Sms.MESSAGE_TYPE_INBOX, contactName)
                            imported++
                        }
                    }
                }
                smsStatus = "بازیابی پیام‌ها انجام شد. $imported پیام بررسی شد."
            } catch (e: SecurityException) {
                smsStatus = "دسترسی خواندن پیامک‌ها داده نشده است."
            } catch (e: Exception) {
                smsStatus = "بازیابی ناموفق بود: ${e.message ?: "خطای نامشخص"}"
            }
        }
    }

    fun clearSmsStatus() { smsStatus = "" }

    fun scheduleSms(conversationId: Long, text: String, scheduledAt: Long) {
        val clean = text.trim()
        if (clean.isEmpty() || scheduledAt <= System.currentTimeMillis()) return
        viewModelScope.launch {
            val conversation = repo.getConversation(conversationId) ?: return@launch
            if (conversation.phoneNumber.isBlank()) { smsStatus = "برای این گفتگو شماره تلفن ثبت نشده است."; return@launch }
            val subId = conversation.simSubscriptionId?.takeIf { it >= 0 } ?: settings.defaultSimSubscriptionId.takeIf { it >= 0 }
            val id = repo.addScheduledSms(conversationId, conversation.phoneNumber, clean, scheduledAt, subId)
            val alarmIntent = Intent(getApplication(), com.example.imessagelocal.ScheduledSmsReceiver::class.java).putExtra("schedule_id", id)
            val pending = PendingIntent.getBroadcast(getApplication(), id.toInt(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            getApplication<Application>().getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledAt, pending)
            smsStatus = "پیام برای زمان انتخاب‌شده زمان‌بندی شد."
        }
    }

    fun cancelScheduledSms(id: Long) { viewModelScope.launch { repo.cancelScheduledSms(id) } }

    fun logIncoming(conversationId: Long, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val name = conversationName(conversationId)
        viewModelScope.launch {
            repo.addMessage(conversationId, clean, isOutgoing = false)
            Notifier.notifyIncoming(getApplication(), name, clean, withSound = settings.sound, soundUri = settings.notificationSoundUri)
        }
    }

    fun editMessage(message: MessageEntity, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch { repo.editMessage(message, clean) }
    }

    fun deleteMessage(message: MessageEntity) {
        viewModelScope.launch { repo.deleteMessage(message) }
    }

    fun toggleMessagePin(message: MessageEntity) {
        viewModelScope.launch { repo.toggleMessagePin(message) }
    }

    fun togglePin(conversation: ConversationEntity) {
        viewModelScope.launch { repo.togglePin(conversation) }
    }

    fun setConversationSim(id: Long, subId: Int?) { viewModelScope.launch { repo.setConversationSim(id, subId) } }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repo.deleteConversation(id)
            if (openId.value == id) openId.value = null
        }
    }
}
