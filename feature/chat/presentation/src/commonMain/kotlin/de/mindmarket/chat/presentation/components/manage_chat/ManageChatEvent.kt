package de.mindmarket.chat.presentation.components.manage_chat

sealed interface ManageChatEvent {
    data object OnMembersAdded: ManageChatEvent
}