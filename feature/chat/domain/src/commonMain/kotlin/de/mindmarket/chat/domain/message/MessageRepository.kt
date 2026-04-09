package de.mindmarket.chat.domain.message

import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.chat.domain.models.MessageWithSender
import de.mindmarket.chat.domain.models.OutgoingNewMessage
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local>

    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError>

    suspend fun sendMessage(message: OutgoingNewMessage): EmptyResult<DataError>

    fun getMessagesForChat(chatId:String): Flow<List<MessageWithSender>>

    suspend fun retryMessage(messageId:String): EmptyResult<DataError>
}