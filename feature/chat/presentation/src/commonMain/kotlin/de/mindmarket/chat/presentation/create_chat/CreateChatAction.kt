package de.mindmarket.chat.presentation.create_chat

sealed interface CreateChatAction {
    data object OnAddClick : CreateChatAction
    data object OnDismissDialog : CreateChatAction
    data object OnCreateChatClick : CreateChatAction
}