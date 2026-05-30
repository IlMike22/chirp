package de.mindmarket.chat.data.mappers

import de.mindmarket.chat.data.dto.ChatParticipantDto
import de.mindmarket.chat.database.entities.ChatParticipantEntity
import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.domain.auth.User

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

fun ChatParticipant.toEntity(): ChatParticipantEntity =
    ChatParticipantEntity(
        userId = userId,
        username = username,
        profilePictureUrl = profilePictureUrl
    )


