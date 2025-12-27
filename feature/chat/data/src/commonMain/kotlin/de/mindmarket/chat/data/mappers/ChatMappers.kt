package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatDto
import de.mindmarket.chat.domain.models.Chat
import kotlin.time.Instant

fun ChatDto.toDomain(): Chat =
    Chat(
        id = id,
        participants = participants.map { it.toDomain() },
        lastActivityAt = Instant.parse(lastActivityAt),
        lastMessage = lastMessage?.toDomain()
    )