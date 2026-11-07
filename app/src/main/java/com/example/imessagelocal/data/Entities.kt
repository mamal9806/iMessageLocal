package com.example.imessagelocal.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String = "",
    val avatarColor: Int,
    val pinned: Boolean = false,
    val lastText: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val text: String,
    val isOutgoing: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val edited: Boolean = false,
    val replyToId: Long? = null,
    val replyPreview: String? = null,
)
