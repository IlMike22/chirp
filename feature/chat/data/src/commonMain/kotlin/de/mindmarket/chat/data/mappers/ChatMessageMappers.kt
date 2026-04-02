package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatMessageDto
import de.mindmarket.chat.data.dto.websocket.IncomingWebSocketDto
import de.mindmarket.chat.data.dto.websocket.OutgoingWebSocket
import de.mindmarket.chat.database.LastMessageView
import de.mindmarket.chat.database.entities.ChatMessageEntity
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.chat.domain.models.OutgoingNewMessage
import kotlin.time.Clock
import kotlin.time.Instant

fun ChatMessageDto.toDomain(): ChatMessage =
    ChatMessage(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = Instant.parse(createdAt),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT
    )

fun ChatMessage.toEntity(): ChatMessageEntity =
    ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name
    )

fun ChatMessage.toLastMessageView(): LastMessageView =
    LastMessageView(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = createdAt.toEpochMilliseconds(),
        deliveryStatus = deliveryStatus.name
    )

fun ChatMessageEntity.toDomain(): ChatMessage =
    ChatMessage(
        id = chatId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.SENT
    )

fun ChatMessage.toNewMessage(): OutgoingWebSocket.NewMessage =
    OutgoingWebSocket.NewMessage(
        messageId = id,
        chatId = chatId,
        content = content,
    )

fun IncomingWebSocketDto.NewMessageDto.toEntity(): ChatMessageEntity =
    ChatMessageEntity(
        messageId = id,
        chatId = chatId,
        senderId = senderId,
        content = content,
        timestamp = Instant.parse(createdAt).toEpochMilliseconds(),
        deliveryStatus = ChatMessageDeliveryStatus.SENT.name
    )

fun OutgoingNewMessage.toWebSocketDto(): OutgoingWebSocket.NewMessage =
    OutgoingWebSocket.NewMessage(
        chatId = chatId,
        messageId = messageId,
        content = content
    )

fun OutgoingWebSocket.NewMessage.toEntity(
    senderId: String,
    deliveryStatus: ChatMessageDeliveryStatus
): ChatMessageEntity =
    ChatMessageEntity(
        messageId = messageId,
        chatId = chatId,
        senderId = senderId,
        content = content,
        deliveryStatus = deliveryStatus.name,
        timestamp = Clock.System.now().toEpochMilliseconds()
    )

