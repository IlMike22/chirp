package de.mindmarket.chat.presentation.create_chat

import de.mindmarket.chat.domain.models.Chat

sealed interface CreateChatEvent {
    data class OnChatCreated(val chat: Chat) : CreateChatEvent
}