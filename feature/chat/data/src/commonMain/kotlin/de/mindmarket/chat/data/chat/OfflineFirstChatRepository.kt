package de.mindmarket.chat.data.chat

import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.data.mappers.toEntity
import de.mindmarket.chat.data.mappers.toLastMessageView
import de.mindmarket.chat.database.ChirpChatDatabase
import de.mindmarket.chat.database.entities.ChatWithParticipants
import de.mindmarket.chat.domain.chat.ChatRepository
import de.mindmarket.chat.domain.chat.ChatService
import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.chat.domain.models.ChatInfo
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.asEmptyResult
import de.mindmarket.core.domain.util.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class OfflineFirstChatRepository(
    private val chatService: ChatService,
    private val db: ChirpChatDatabase
) : ChatRepository {
    override fun getChats(): Flow<List<Chat>> {
        return db.chatDao.getChatsWithActiveParticipants()
            .map { chatWithParticipantsList ->
                chatWithParticipantsList.map { it.toDomain() }
            }
    }

    override fun getChatInfoById(chatId: String): Flow<ChatInfo> {
        return db.chatDao.getChatInfoById(chatId)
            .filterNotNull()
            .map { it.toDomain() }
    }

    override suspend fun fetchChats(): Result<List<Chat>, DataError.Remote> {
        return chatService
            .getChats()
            .onSuccess { chats ->
                val chatsWithParticipants = chats.map { chat ->
                    ChatWithParticipants(
                        chat = chat.toEntity(),
                        participants = chat.participants.map { it.toEntity() },
                        lastMessage = chat.lastMessage?.toLastMessageView()
                    )
                }

                db.chatDao.upsertChatsParticipantsAndCrossRefs(
                    chats = chatsWithParticipants,
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao,
                    messageDao = db.chatMessageDao
                )
            }
    }

    override suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote> {
        return chatService.getChatById(chatId)
            .onSuccess { chat ->
                db.chatDao.upsertChatParticipantsAndCrossRefs(
                    chat = chat.toEntity(),
                    participants = chat.participants.map { it.toEntity() },
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao
                )
            }
            .asEmptyResult()
    }

    override suspend fun createChat(otherUserIds: List<String>): Result<Chat, DataError.Remote> {
        return chatService.createChat(otherUserIds = otherUserIds)
            .onSuccess { chat ->
                db.chatDao.upsertChatParticipantsAndCrossRefs(
                    chat = chat.toEntity(),
                    participants = chat.participants.map { it.toEntity() },
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao
                )
            }
    }
}