package com.example.imessagelocal.ui

import android.app.Application
import android.telephony.SmsManager
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

    fun clearSmsStatus() { smsStatus = "" }

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
