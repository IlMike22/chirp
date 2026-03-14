package de.mindmarket.chat.data.dto.websocket

import kotlinx.serialization.Serializable

enum class OutgoingWebSocketType {
    NEW_MESSAGE
}

@Serializable
sealed class OutgoingWebSocket(
    val type: OutgoingWebSocketType
) {
    @Serializable
    data class NewMessage(
        val chatId: String,
        val messageId: String,
        val content: String
    ) : OutgoingWebSocket(OutgoingWebSocketType.NEW_MESSAGE)
}