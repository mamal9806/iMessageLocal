package com.example.imessagelocal.ui

import android.app.Application
import android.telephony.SmsManager
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
            try {
                val conversation = repo.getConversation(conversationId)
                val number = conversation?.phoneNumber.orEmpty()
                if (number.isBlank()) {
                    smsStatus = "برای این گفتگو شماره تلفن ثبت نشده است."
                    return@launch
                }
                @Suppress("DEPRECATION")
                val manager = SmsManager.getDefault()
                val parts = manager.divideMessage(clean)
                if (parts.size > 1) manager.sendMultipartTextMessage(number, null, parts, null, null)
                else manager.sendTextMessage(number, null, clean, null, null)
                repo.addMessage(conversationId, clean, isOutgoing = true, replyTo = replyTo)
                smsStatus = "پیام به برنامه پیامک اپراتور سپرده شد؛ تحویل قطعی تأیید نشده است."
            } catch (e: Exception) {
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
            Notifier.notifyIncoming(getApplication(), name, clean, withSound = settings.sound)
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

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repo.deleteConversation(id)
            if (openId.value == id) openId.value = null
        }
    }
}
