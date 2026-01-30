package de.mindmarket.chat.domain.chat

import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result

interface ChatService {
    suspend fun createChat(
        otherUserIds: List<String>
    ): Result<Chat, DataError.Remote>

    suspend fun getChats(): Result<List<Chat>, DataError.Remote>
    suspend fun getChatById(chatId:String): Result<Chat, DataError.Remote>
}