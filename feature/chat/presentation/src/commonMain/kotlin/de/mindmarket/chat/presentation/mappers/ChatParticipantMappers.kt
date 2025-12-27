package de.mindmarket.chat.presentation.mappers

import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi

fun ChatParticipant.toUi(): ChatParticipantUi =
    ChatParticipantUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )