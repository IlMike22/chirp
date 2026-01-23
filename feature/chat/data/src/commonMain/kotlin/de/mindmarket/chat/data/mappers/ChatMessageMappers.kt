package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatMessageDto
import de.mindmarket.chat.database.LastMessageView
import de.mindmarket.chat.database.entities.ChatMessageEntity
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
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