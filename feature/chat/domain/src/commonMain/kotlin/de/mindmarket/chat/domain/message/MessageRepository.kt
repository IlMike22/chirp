package de.mindmarket.chat.domain.message

import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult

interface MessageRepository {
    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>
}