package de.mindmarket.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.mindmarket.chat.database.entities.ChatEntity
import de.mindmarket.chat.database.entities.ChatInfoEntity
import de.mindmarket.chat.database.entities.ChatMessageEntity
import de.mindmarket.chat.database.entities.ChatParticipantCrossRef
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
    @Transaction
    fun getChatsWithParticipants(): Flow<List<ChatWithParticipants>>

    @Query(
        """
        SELECT DISTINCT c.*
        FROM chatentity c 
        JOIN chatparticipantcrossref cpcr ON c.chatId = cpcr.chatId
        WHERE cpcr.isActive = 1
        ORDER BY lastActivityAt DESC
    """
    )
    @Transaction
    fun getChatsWithActiveParticipants(): Flow<List<ChatWithParticipants>>

    @Query("SELECT * FROM ChatEntity WHERE chatId = :id")
    @Transaction
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

    @Query(
        """
        SELECT p.* 
        FROM ChatParticipantEntity p
        JOIN ChatParticipantCrossRef cpcr ON p.userId = cpcr.userId
        WHERE cpcr.chatId = :chatId AND cpcr.isActive = true
        ORDER BY p.username
    """
    )
    fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipantEntity>>

    @Query("""
        SELECT c.* 
        FROM chatentity c
        JOIN chatparticipantcrossref cpcr ON c.chatId = cpcr.chatId
        WHERE c.chatId = :chatId AND cpcr.isActive = true
    """)
    @Transaction
    fun getChatInfoById(chatId: String): Flow<ChatInfoEntity?>

    @Transaction
    suspend fun upsertChatParticipantsAndCrossRefs(
        chat: ChatEntity,
        participants: List<ChatParticipantEntity>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantsCrossRefDao
    ) {
        upsertChat(chat)
        participantDao.upsertParticipants(participants)
        val crossRefs = participants.map { participant ->
            ChatParticipantCrossRef(
                chatId = chat.chatId,
                userId = participant.userId,
                isActive = true
            )
        }
        crossRefDao.upsertCrossRefs(crossRefs)
        crossRefDao.syncParticipants(chat.chatId, participants)
    }

    @Transaction
    suspend fun upsertChatsParticipantsAndCrossRefs(
        chats: List<ChatWithParticipants>,
        participantDao: ChatParticipantDao,
        crossRefDao: ChatParticipantsCrossRefDao,
        messageDao: ChatMessageDao
    ) {
        upsertChats(chats.map { it.chat })

        val localChatIds = getAllChatIds()
        val serverChatIds = chats.map { it.chat.chatId }
        val staleChatIds = localChatIds - serverChatIds

        chats.forEach { chat ->
            chat.lastMessage?.run {
                messageDao.upsertMessage(
                    ChatMessageEntity(
                        messageId = messageId,
                        chatId = chatId,
                        senderId = senderId,
                        content = content,
                        timestamp = timestamp,
                        deliveryStatus = deliveryStatus
                    )
                )
            }
        }

        val allParticipants = chats.flatMap { it.participants }
        participantDao.upsertParticipants(allParticipants)

        val allCrossRefs = chats.flatMap { chatWithParticipants ->
            chatWithParticipants.participants.map { participant ->
                ChatParticipantCrossRef(
                    chatId = chatWithParticipants.chat.chatId,
                    userId = participant.userId,
                    isActive = true
                )
            }
        }

        crossRefDao.upsertCrossRefs(allCrossRefs)

        chats.forEach { chat ->
            crossRefDao.syncParticipants(
                chatId = chat.chat.chatId,
                participants = chat.participants
            )
        }

        deleteChatsByIds(staleChatIds)
    }
}