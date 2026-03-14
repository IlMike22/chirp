package de.mindmarket.chat.data.chat

import de.mindmarket.chat.data.dto.websocket.WebSocketMessageDto
import de.mindmarket.chat.data.mappers.toNewMessage
import de.mindmarket.chat.data.network.KtorWebSocketConnector
import de.mindmarket.chat.database.ChirpChatDatabase
import de.mindmarket.chat.domain.chat.ChatConnectionClient
import de.mindmarket.chat.domain.chat.ChatRepository
import de.mindmarket.chat.domain.error.ConnectionError
import de.mindmarket.chat.domain.message.MessageRepository
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.core.domain.auth.SessionStorage
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class WebSocketChatConnectionClient(
    private val webSocketConnector: KtorWebSocketConnector,
    private val chatRepository: ChatRepository,
    private val database: ChirpChatDatabase,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val messageRepository: MessageRepository
) : ChatConnectionClient {
    override val chatMessages: Flow<ChatMessage>
        get() = TODO("Not yet implemented")
    override val connectionState = webSocketConnector.connectionState

    override suspend fun sendChatMessage(message: ChatMessage): EmptyResult<ConnectionError> {
        val outgoingDto = message.toNewMessage()
        val webSocketMessage = WebSocketMessageDto(
            type = outgoingDto.type.name,
            payload = json.encodeToString(outgoingDto)
        )

        val rawJsonPayload = json.encodeToString(webSocketMessage)

        return webSocketConnector
            .sendMessage(rawJsonPayload)
            .onFailure { error ->
                messageRepository.updateMessageDeliveryStatus(
                    messageId = message.id,
                    status = ChatMessageDeliveryStatus.FAILED
                )
            }
    }
}