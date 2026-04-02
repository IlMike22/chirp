package de.mindmarket.chat.data.message

import de.mindmarket.chat.data.dto.websocket.OutgoingWebSocket
import de.mindmarket.chat.data.dto.websocket.WebSocketMessageDto
import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.data.mappers.toEntity
import de.mindmarket.chat.data.mappers.toWebSocketDto
import de.mindmarket.chat.data.message.ChatMessageConstants.PAGE_SIZE
import de.mindmarket.chat.data.network.KtorWebSocketConnector
import de.mindmarket.chat.database.ChirpChatDatabase
import de.mindmarket.chat.domain.message.ChatMessageService
import de.mindmarket.chat.domain.message.MessageRepository
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.chat.domain.models.MessageWithSender
import de.mindmarket.chat.domain.models.OutgoingNewMessage
import de.mindmarket.core.data.database.safeDatabaseUpdate
import de.mindmarket.core.domain.auth.SessionStorage
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.onFailure
import de.mindmarket.core.domain.util.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class OfflineFirstMessageRepository(
    private val database: ChirpChatDatabase,
    private val service: ChatMessageService,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val webSocketConnector: KtorWebSocketConnector,
    private val applicationScope: CoroutineScope
) : MessageRepository {
    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            database.chatMessageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError> {
        return service.fetchMessages(chatId, before)
            .onSuccess { messages ->
                return safeDatabaseUpdate {
                    database.chatMessageDao.upsertMessagesAndSyncIfNecessary(
                        chatId = chatId,
                        serverMessages = messages.map { it.toEntity() },
                        pageSize = PAGE_SIZE,
                        shouldSync = before == null, // only sync for most recent page
                    )
                    messages
                }
            }
    }

    override suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError> {
        return safeDatabaseUpdate {
            val dto = message.toWebSocketDto()
            val localUser = sessionStorage.observeAuthInfo().first()?.user
                ?: return Result.Failure(DataError.Local.NOT_FOUND)
            val entity = dto.toEntity(
                senderId = localUser.id,
                deliveryStatus = ChatMessageDeliveryStatus.SENDING
            )
            database.chatMessageDao.upsertMessage(entity)

            return webSocketConnector
                .sendMessage(dto.toJsonPayload())
                .onFailure { error ->
                    applicationScope.launch {
                        database.chatMessageDao.upsertMessage(
                            dto.toEntity(
                                senderId = localUser.id,
                                deliveryStatus = ChatMessageDeliveryStatus.FAILED
                            )
                        )
                    }.join()
                }
        }
    }

    override fun getMessagesForChat(chatId: String): Flow<List<MessageWithSender>> {
        return database.chatMessageDao.getMessagesByChatId(chatId).map { messages ->
            messages.map {
                it.toDomain()
            }
        }
    }

    private fun OutgoingWebSocket.NewMessage.toJsonPayload(): String {

        val webSocketMessage = WebSocketMessageDto(
            type = type.name,
            payload = json.encodeToString(this)
        )

        return json.encodeToString(webSocketMessage)
    }
}