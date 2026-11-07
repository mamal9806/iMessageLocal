package com.example.imessagelocal.data

class ChatRepository(private val dao: ChatDao) {

    fun conversations(query: String) =
        if (query.isBlank()) dao.observeConversations() else dao.searchConversations(query.trim())

    fun messages(conversationId: Long) = dao.observeMessages(conversationId)

    suspend fun createConversation(name: String): Long =
        dao.insertConversation(ConversationEntity(name = name, avatarColor = AVATAR_COLORS.random()))

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
