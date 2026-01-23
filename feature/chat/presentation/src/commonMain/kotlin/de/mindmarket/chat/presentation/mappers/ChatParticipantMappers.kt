package de.mindmarket.chat.presentation.mappers

import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi
import de.mindmarket.core.domain.auth.User

fun ChatParticipant.toUi(): ChatParticipantUi =
    ChatParticipantUi(
        id = userId,
        username = username,
        initials = initials,
        imageUrl = profilePictureUrl
    )

fun User.toUi(): ChatParticipantUi =
    ChatParticipantUi(
        id = id,
        username = username,
        initials = username.take(2).uppercase(),
        imageUrl = profilePictureUrl
    )