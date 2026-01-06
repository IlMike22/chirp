package de.mindmarket.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.mindmarket.chat.database.entities.ChatEntity
import de.mindmarket.chat.database.entities.ChatInfoEntity
import de.mindmarket.chat.database.entities.ChatParticipantEntity
import de.mindmarket.chat.database.entities.ChatWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Upsert
    suspend fun upsertChat(chat: ChatEntity)

    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)

    @Query("DELETE FROM chatentity WHERE chatId = :chatId ")
    suspend fun deleteChatById(chatId: String)

    @Query("SELECT * FROM chatentity ORDER BY lastActivityAt DESC")
    fun getChatsParticipants(): Flow<List<ChatWithParticipants>>

    @Query("SELECT * FROM ChatEntity WHERE chatId = :id")
    suspend fun getChatById(id: String): ChatWithParticipants?

    @Query("DELETE FROM ChatEntity")
    suspend fun deleteAllChats()

    @Query("SELECT chatId from chatentity")
    suspend fun getAllChatIds(): List<String>

    @Transaction
    suspend fun deleteChatsByIds(chatIds: List<String>) {
        chatIds.forEach { chatId ->
            deleteChatById(chatId)
        }
    }

    @Query("SELECT COUNT(*) FROM chatentity")
    fun getChatCount(): Flow<Int>

    @Query("""
        SELECT p.* 
        FROM ChatParticipantEntity p
        JOIN ChatParticipantCrossRef cpcr ON p.userId = cpcr.userId
        WHERE cpcr.chatId = :chatId AND cpcr.isActive = true
        ORDER BY p.username
    """)
    fun getActiveParticipantsByChatId(chatId:String): Flow<List<ChatParticipantEntity>>

    @Query("SELECT * FROM chatentity WHERE chatId = :chatId")
    fun getChatInfoById(chatId:String): Flow<ChatInfoEntity?>
}