package de.mindmarket.chat.presentation.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mindmarket.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class ChatListViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {
    private val _state = MutableStateFlow(ChatListState())
    val state = _state.onStart {

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatListState()
    )

    fun onAction(action: ChatListAction) {
        when (action) {
            else -> Unit
        }
    }

}