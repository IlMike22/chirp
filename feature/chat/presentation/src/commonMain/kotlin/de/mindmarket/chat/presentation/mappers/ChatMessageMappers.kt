package de.mindmarket.chat.presentation.mappers

import de.mindmarket.chat.domain.models.MessageWithSender
import de.mindmarket.chat.presentation.model.MessageUi
import de.mindmarket.chat.presentation.util.DateUtils
import kotlinx.serialization.json.JsonNull.content

fun MessageWithSender.toUi(
    localUserId: String
): MessageUi {
    val isFromLocalUser = this.sender.userId == localUserId
    return if (isFromLocalUser) {
        MessageUi.LocalUserMessage(
            id = message.id,
            content = content,
            deliveryStatus = message.deliveryStatus,
            formattedSentTime = DateUtils.formatMessageTime(
                instant = message.createdAt
            )
        )
    } else {
        MessageUi.OtherUserMessage(
            id = message.id,
            content = content,
            formattedSentTime = DateUtils.formatMessageTime(instant = message.createdAt),
            sender = sender.toUi()
        )
    }
}

