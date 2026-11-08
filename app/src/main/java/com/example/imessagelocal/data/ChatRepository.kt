package com.example.imessagelocal.data

import android.telephony.PhoneNumberUtils

class ChatRepository(private val dao: ChatDao) {

    fun conversations(query: String) =
        if (query.isBlank()) dao.observeConversations() else dao.searchConversations(query.trim())

    fun messages(conversationId: Long) = dao.observeMessages(conversationId)

    suspend fun createConversation(name: String, phoneNumber: String): Long {
        val cleanNumber = phoneNumber.trim()
        val existing = dao.getAllConversations().firstOrNull {
            cleanNumber.isNotBlank() && it.phoneNumber.isNotBlank() &&
                PhoneNumberUtils.compare(it.phoneNumber, cleanNumber)
        }
        if (existing != null) {
            if (name.isNotBlank() && existing.name != name) dao.updateConversation(existing.copy(name = name))
            return existing.id
        }
        return dao.insertConversation(ConversationEntity(name = name, phoneNumber = cleanNumber, avatarColor = AVATAR_COLORS.random()))
    }

    suspend fun getConversation(id: Long) = dao.getConversation(id)

    fun scheduledMessages(conversationId: Long) = dao.observeScheduled(conversationId)

    suspend fun addScheduledSms(conversationId: Long, phoneNumber: String, text: String, scheduledAt: Long, subId: Int?): Long =
        dao.insertScheduled(ScheduledSmsEntity(conversationId = conversationId, phoneNumber = phoneNumber, text = text, scheduledAt = scheduledAt, simSubscriptionId = subId))

    suspend fun cancelScheduledSms(id: Long) = dao.updateScheduledStatus(id, "CANCELLED")

    suspend fun receiveSms(address: String, body: String, timestamp: Long, contactName: String? = null): Long {
        val number = address.trim()
        var conversation = dao.getAllConversations().firstOrNull {
            it.phoneNumber.isNotBlank() && PhoneNumberUtils.compare(it.phoneNumber, number)
        }
        if (conversation == null) {
            val id = dao.insertConversation(ConversationEntity(name = number, phoneNumber = number, avatarColor = AVATAR_COLORS.random()))
            conversation = dao.getConversation(id)!!
        }
        if (!contactName.isNullOrBlank() && conversation.name != contactName) {
            conversation = conversation.copy(name = contactName)
            dao.updateConversation(conversation)
        }
        if (!dao.hasIncomingDuplicate(conversation.id, body, timestamp - 5000, timestamp + 5000)) {
            dao.insertMessage(MessageEntity(conversationId = conversation.id, text = body, isOutgoing = false, createdAt = timestamp, deliveryState = "RECEIVED"))
            dao.updateConversation(conversation.copy(lastText = body, updatedAt = timestamp))
        }
        return conversation.id
    }

    suspend fun importSms(address: String, body: String, timestamp: Long, outgoing: Boolean) {
        val number = address.trim()
        if (number.isBlank() || body.isBlank()) return
        val existing = dao.getAllConversations().firstOrNull {
            it.phoneNumber.isNotBlank() && PhoneNumberUtils.compare(it.phoneNumber, number)
        }
        val conversationId = existing?.id ?: createConversation(number, number)
        if (dao.hasImportedDuplicate(conversationId, body, outgoing, timestamp)) return
        dao.insertMessage(MessageEntity(conversationId = conversationId, text = body, isOutgoing = outgoing, createdAt = timestamp, deliveryState = if (outgoing) "SENT" else "RECEIVED"))
        val conversation = dao.getConversation(conversationId) ?: return
        if (conversation.lastText.isBlank() || timestamp >= conversation.updatedAt) dao.updateConversation(conversation.copy(lastText = body, updatedAt = timestamp))
    }

    suspend fun recordOutgoing(conversationId: Long, text: String, subId: Int?): Long {
        val id = dao.insertMessage(MessageEntity(conversationId = conversationId, text = text, isOutgoing = true, deliveryState = "SENDING", simSubscriptionId = subId))
        dao.getConversation(conversationId)?.let { dao.updateConversation(it.copy(lastText = text, updatedAt = System.currentTimeMillis())) }
        return id
    }

    suspend fun updateDelivery(id: Long, state: String) = dao.updateDeliveryState(id, state)
    suspend fun setConversationSim(id: Long, subId: Int?) = dao.setConversationSim(id, subId)

    suspend fun addMessage(
        conversationId: Long,
        text: String,
        isOutgoing: Boolean,
        replyTo: MessageEntity? = null,
    ) {
        dao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                text = text,
                isOutgoing = isOutgoing,
                replyToId = replyTo?.id,
                replyPreview = replyTo?.text?.take(80),
            ),
        )
        val conversation = dao.getConversation(conversationId) ?: return
        dao.updateConversation(
            conversation.copy(lastText = text, updatedAt = System.currentTimeMillis()),
        )
    }

    suspend fun editMessage(message: MessageEntity, text: String) = dao.editMessage(message.id, text)

    suspend fun deleteMessage(message: MessageEntity) = dao.deleteMessage(message.id)

    suspend fun togglePin(conversation: ConversationEntity) =
        dao.updateConversation(conversation.copy(pinned = !conversation.pinned))

    suspend fun deleteConversation(id: Long) = dao.deleteConversation(id)

    companion object {
        val AVATAR_COLORS = listOf(
            0xFFE57373.toInt(),
            0xFF81C784.toInt(),
            0xFF64B5F6.toInt(),
            0xFFBA68C8.toInt(),
            0xFFFFB74D.toInt(),
        )
    }
}
