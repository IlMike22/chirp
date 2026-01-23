package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatDto
import de.mindmarket.chat.database.LastMessageView
import de.mindmarket.chat.database.entities.ChatEntity
import de.mindmarket.chat.database.entities.ChatParticipantEntity
import de.mindmarket.chat.database.entities.ChatWithParticipants
import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.chat.domain.models.ChatParticipant
import kotlin.time.Instant

fun ChatDto.toDomain(): Chat =
    Chat(
        id = id,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.parse(lastActivityAt),
        lastMessage = lastMessage?.toDomain()
    )

fun ChatWithParticipants.toDomain(): Chat {
    return Chat(
        id = chat.chatId,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.fromEpochMilliseconds(chat.lastActivityAt),
        lastMessage = lastMessage?.toDomain()
    )
}

fun ChatParticipantEntity.toDomain(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )
}

fun LastMessageView.toDomain(): ChatMessage =
    ChatMessage(
        id = messageId,
        chatId = chatId,
        content = content,
        createdAt = Instant.fromEpochMilliseconds(timestamp),
        senderId = senderId,
        deliveryStatus = ChatMessageDeliveryStatus.valueOf(this.deliveryStatus)
    )

fun Chat.toEntity(): ChatEntity =
    ChatEntity(
        chatId = id,
        lastActivityAt = lastActivityAt.toEpochMilliseconds()
    )
