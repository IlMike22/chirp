package de.mindmarket.chat.presentation.chat_list

import de.mindmarket.chat.presentation.model.ChatUi

sealed interface ChatListAction {
    data object OnUserAvatarClick : ChatListAction
    data object OnDismissUserMenu : ChatListAction
    data object OnLogoutClick : ChatListAction
    data object OnConfirmLogoutClick : ChatListAction
    data object OnDismissLogoutDialog : ChatListAction
    data object OnCreateChatClick: ChatListAction
    data object OnProfileSettingsClick: ChatListAction
    data class OnChatClick(val chat: ChatUi) : ChatListAction
}