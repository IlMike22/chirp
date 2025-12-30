package de.mindmarket.chat.presentation.chat_list

import de.mindmarket.chat.presentation.model.ChatUi
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi
import de.mindmarket.core.presentation.util.UiText

data class ChatListState(
    val chats: List<ChatUi> = emptyList(),
    val error: UiText? = null,
    val localParticipant: ChatParticipantUi? = null,
    val isUserMenuOpen: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val selectedChatId: String? = null,
    val isLoading: Boolean = false
)
