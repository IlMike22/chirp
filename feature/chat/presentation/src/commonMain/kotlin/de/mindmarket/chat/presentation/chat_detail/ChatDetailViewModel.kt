package de.mindmarket.chat.presentation.chat_detail

import androidx.lifecycle.ViewModel
import de.mindmarket.chat.presentation.chat_detail.components.ChatDetailAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatDetailViewModel(): ViewModel() {
    private val _state = MutableStateFlow(ChatDetailState())
    val state = _state.asStateFlow()

    fun onAction(action: ChatDetailAction) {

    }
}