package de.mindmarket.chat.domain.chat

import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.chat.domain.models.ChatInfo
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    fun getChatInfoById(chatId:String):Flow<ChatInfo>
    suspend fun fetchChats(): Result<List<Chat>, DataError.Remote>
    suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote>
}