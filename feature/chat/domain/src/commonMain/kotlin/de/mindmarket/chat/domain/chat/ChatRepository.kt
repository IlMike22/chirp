package de.mindmarket.chat.domain.chat

import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChats(): Flow<List<Chat>>
    suspend fun fetchChats(): Result<List<Chat>, DataError.Remote>
}