package com.example.imessagelocal.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "''") val phoneNumber: String = "",
    val avatarColor: Int,
    val pinned: Boolean = false,
    val lastText: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val simSubscriptionId: Int? = null,
    @ColumnInfo(defaultValue = "0") val archived: Boolean = false,
    @ColumnInfo(defaultValue = "1") val notificationsEnabled: Boolean = true,
    val contactUri: String? = null,
    @ColumnInfo(defaultValue = "0") val unreadCount: Int = 0,
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
    @ColumnInfo(defaultValue = "'LOCAL'") val deliveryState: String = "LOCAL",
    val simSubscriptionId: Int? = null,
)
