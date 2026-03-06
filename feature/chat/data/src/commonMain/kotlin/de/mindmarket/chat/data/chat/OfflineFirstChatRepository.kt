package de.mindmarket.chat.data.chat

import de.mindmarket.chat.data.lifecycle.AppLifecycleObserver
import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.data.mappers.toEntity
import de.mindmarket.chat.data.mappers.toLastMessageView
import de.mindmarket.chat.data.network.ConnectivityObserver
import de.mindmarket.chat.database.ChirpChatDatabase
import de.mindmarket.chat.database.entities.ChatInfoEntity
import de.mindmarket.chat.database.entities.ChatParticipantEntity
import de.mindmarket.chat.database.entities.ChatWithParticipants
import de.mindmarket.chat.domain.chat.ChatRepository
import de.mindmarket.chat.domain.chat.ChatService
import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.chat.domain.models.ChatInfo
import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.asEmptyResult
import de.mindmarket.core.domain.util.onSuccess
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope

class OfflineFirstChatRepository(
    private val chatService: ChatService,
    private val db: ChirpChatDatabase,
    private val observer: ConnectivityObserver
) : ChatRepository {

    init {
        observer.isConnected.onEach { isConnected ->
            println("Is app connected? $isConnected")
        }.launchIn(GlobalScope)
    }

    override fun getChats(): Flow<List<Chat>> {
        return db.chatDao.getChatsWithParticipants()
            .map { allChatsWithParticipants ->
                supervisorScope {
                    allChatsWithParticipants
                        .map { chatWithParticipants ->
                            async {
                                ChatWithParticipants(
                                    chat = chatWithParticipants.chat,
                                    participants = chatWithParticipants
                                        .participants
                                        .onlyActive(chatWithParticipants.chat.chatId),
                                    lastMessage = chatWithParticipants.lastMessage
                                )
                            }
                        }
                        .awaitAll()
                        .map { it.toDomain() }
                }
            }
    }

    override fun getChatInfoById(chatId: String): Flow<ChatInfo> {
        return db.chatDao.getChatInfoById(chatId)
            .filterNotNull()
            .map { chatInfo ->
                ChatInfoEntity(
                    chat = chatInfo.chat,
                    participants = chatInfo
                        .participants
                        .onlyActive(chatInfo.chat.chatId),
                    messagesWithSenders = chatInfo.messagesWithSenders
                )
            }
            .map { it.toDomain() }
    }

    override fun getActiveParticipantsByChatId(chatId: String): Flow<List<ChatParticipant>> {
        return db.chatDao.getActiveParticipantsByChatId(chatId).map {participants ->
            participants.map { it.toDomain() }
        }
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

    override suspend fun leaveChat(chatId: String): EmptyResult<DataError.Remote> {
        return chatService
            .leaveChat(chatId)
            .onSuccess {
                db.chatDao.deleteChatById(chatId)
            }
    }

    override suspend fun addParticipantsToChat(
        chatId: String,
        userIds: List<String>
    ): Result<Chat, DataError.Remote> {
        return chatService
            .addParticipantsToChat(
                chatId = chatId,
                userIds = userIds
            )
            .onSuccess { chat ->
                db.chatDao.upsertChatParticipantsAndCrossRefs(
                    chat = chat.toEntity(),
                    participants = chat.participants.map { it.toEntity() },
                    participantDao = db.chatParticipantDao,
                    crossRefDao = db.chatParticipantsCrossRefDao
                )
            }
    }

    private suspend fun List<ChatParticipantEntity>.onlyActive(chatId: String): List<ChatParticipantEntity> {
        val activeParticipantsIds = db
            .chatDao
            .getActiveParticipantsByChatId(chatId)
            .first()
            .map { it.userId }

        return this.filter { it.userId in activeParticipantsIds }
    }
}