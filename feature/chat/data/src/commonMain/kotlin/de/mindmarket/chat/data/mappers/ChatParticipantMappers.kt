package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatParticipantDto
import de.mindmarket.chat.domain.models.ChatParticipant

fun ChatParticipantDto.toDomain(): ChatParticipant =
    ChatParticipant(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )

fun ChatParticipant.toData(): ChatParticipantDto =
    ChatParticipantDto(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )