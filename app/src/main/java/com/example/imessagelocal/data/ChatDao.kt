package com.example.imessagelocal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY pinned DESC, updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY pinned DESC, updatedAt DESC")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: Long)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET text = :text, edited = 1 WHERE id = :id")
    suspend fun editMessage(id: Long, text: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)
}
