package com.example.imessagelocal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations WHERE archived = 0 ORDER BY pinned DESC, updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE archived = 0 AND (name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' OR lastText LIKE '%' || :query || '%') ORDER BY pinned DESC, updatedAt DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations")
    suspend fun getAllConversations(): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE phoneNumber = :number LIMIT 1")
    suspend fun findByPhone(number: String): ConversationEntity?

    @Query("UPDATE conversations SET simSubscriptionId = :subId WHERE id = :id")
    suspend fun setConversationSim(id: Long, subId: Int?)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE conversationId=:conversationId AND text=:text AND isOutgoing=:isOutgoing AND createdAt=:timestamp)")
    suspend fun hasImportedDuplicate(conversationId: Long, text: String, isOutgoing: Boolean, timestamp: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE conversationId=:conversationId AND text=:text AND isOutgoing=0 AND createdAt BETWEEN :from AND :to)")
    suspend fun hasIncomingDuplicate(conversationId: Long, text: String, from: Long, to: Long): Boolean

    @Query("UPDATE messages SET deliveryState = :state WHERE id = :id")
    suspend fun updateDeliveryState(id: Long, state: String)

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET text = :text, edited = 1 WHERE id = :id")
    suspend fun editMessage(id: Long, text: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("UPDATE messages SET pinned = :pinned WHERE id = :id")
    suspend fun setMessagePinned(id: Long, pinned: Boolean)

    @Insert
    suspend fun insertScheduled(item: ScheduledSmsEntity): Long

    @Query("SELECT * FROM scheduled_sms WHERE conversationId = :conversationId AND status = 'PENDING' ORDER BY scheduledAt ASC")
    fun observeScheduled(conversationId: Long): Flow<List<ScheduledSmsEntity>>

    @Query("SELECT * FROM scheduled_sms WHERE id = :id")
    suspend fun getScheduled(id: Long): ScheduledSmsEntity?

    @Query("UPDATE scheduled_sms SET status = :status WHERE id = :id")
    suspend fun updateScheduledStatus(id: Long, status: String)
}
