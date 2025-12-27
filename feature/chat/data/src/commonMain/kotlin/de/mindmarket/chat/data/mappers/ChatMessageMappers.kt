package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatMessageDto
import de.mindmarket.chat.domain.models.ChatMessage
import kotlin.time.Instant

fun ChatMessageDto.toDomain(): ChatMessage =
    ChatMessage(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = Instant.parse(createdAt),
        senderId = senderId
    )