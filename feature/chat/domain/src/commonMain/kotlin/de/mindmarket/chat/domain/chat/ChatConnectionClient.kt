package de.mindmarket.chat.domain.chat

import de.mindmarket.chat.domain.error.ConnectionError
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ConnectionState
import de.mindmarket.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatConnectionClient {
    val chatMessages: Flow<ChatMessage>
    val connectionState: StateFlow<ConnectionState>
    suspend fun sendChatMessage(message:ChatMessage): EmptyResult<ConnectionError>
}