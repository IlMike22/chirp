package de.mindmarket.chat.domain.message

import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result

interface ChatMessageService {
    suspend fun fetchMessages(
        chatId: String,
        before: String? = null
    ): Result<List<ChatMessage>, DataError.Remote>

    suspend fun deleteMessage(messageId:String): EmptyResult<DataError.Remote>
}