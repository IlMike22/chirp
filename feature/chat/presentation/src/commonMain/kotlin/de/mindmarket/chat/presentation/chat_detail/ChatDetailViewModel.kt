package de.mindmarket.chat.presentation.chat_detail

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mindmarket.chat.domain.chat.ChatConnectionClient
import de.mindmarket.chat.domain.chat.ChatRepository
import de.mindmarket.chat.domain.message.MessageRepository
import de.mindmarket.chat.domain.models.ConnectionState
import de.mindmarket.chat.domain.models.OutgoingNewMessage
import de.mindmarket.chat.presentation.chat_detail.components.ChatDetailAction
import de.mindmarket.chat.presentation.mappers.toUi
import de.mindmarket.core.domain.auth.SessionStorage
import de.mindmarket.core.domain.util.onFailure
import de.mindmarket.core.domain.util.onSuccess
import de.mindmarket.core.presentation.util.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatDetailViewModel(
    private val chatRepository: ChatRepository,
    private val sessionStorage: SessionStorage,
    private val messageRepository: MessageRepository,
    private val connectionClient: ChatConnectionClient
) : ViewModel() {
    private var hasLoadedInitialData = false

    private val eventChannel = Channel<ChatDetailEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _chatId = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow(ChatDetailState())

    private val canSendMessage = snapshotFlow { state.value.messageTextFieldState.text.toString() }
        .map { it.isBlank() }
        .combine(connectionClient.connectionState) { isMessageBlank, connectionState ->
            !isMessageBlank && connectionState == ConnectionState.CONNECTED
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val chatInfoFlow = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                chatRepository
                    .getChatInfoById(chatId)
            } else emptyFlow()
        }

    private val stateWithMessages = combine(
        _state,
        chatInfoFlow,
        sessionStorage.observeAuthInfo()
    ) { currentState, chatInfo, authInfo ->
        if (authInfo == null) {
            return@combine ChatDetailState()
        }
        currentState.copy(
            chatUi = chatInfo.chat.toUi(authInfo.user.id)
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = _chatId
        .flatMapLatest { chatId ->
            if (chatId != null) {
                stateWithMessages
            } else {
                _state
            }
        }
        .onStart {
            if (!hasLoadedInitialData) {
                observeConnectionState()
                observeChatMessages()
                observeCanSendMessage()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ChatDetailState()
        )

    fun onAction(action: ChatDetailAction) {
        when (action) {
            is ChatDetailAction.OnSelectChat -> switchChat(action.chatId)
            ChatDetailAction.OnBackClick -> {}
            ChatDetailAction.OnChatMembersClick -> {}
            ChatDetailAction.OnChatOptionsClick -> onChatOptionsClick()
            is ChatDetailAction.OnDeleteMessageClick -> {}
            ChatDetailAction.OnDismissChatOptionsClick -> onDismissChatOptionsClick()
            ChatDetailAction.OnDismissMessageMenu -> {}
            ChatDetailAction.OnLeaveChatClick -> onLeaveChatClick()
            is ChatDetailAction.OnMessageLongClick -> {}
            is ChatDetailAction.OnRetryClick -> {}
            ChatDetailAction.OnScrollToTop -> {}
            ChatDetailAction.OnSendMessageClick -> sendMessage()
        }
    }

    private fun observeCanSendMessage() {
        canSendMessage
            .onEach { canSend ->
                _state.update { it.copy(
                    canSendMessage = canSend
                ) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun sendMessage() {
        val currentChatId = _chatId.value
        val content = _state.value.messageTextFieldState.text.toString().trim()
        if (content.isBlank() || currentChatId == null) {
            return
        }

        viewModelScope.launch {
            val message = OutgoingNewMessage(
                chatId = currentChatId,
                messageId = Uuid.random().toString(),
                content = content
            )

            messageRepository
                .sendMessage(message)
                .onSuccess {
                    state.value.messageTextFieldState.clearText()
                }
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeChatMessages() {
        val currentMessages = state.map { it.messages }
            .distinctUntilChanged()

        val newMessages = _chatId
            .flatMapLatest { chatId ->
                if (chatId != null) {
                    messageRepository
                        .getMessagesForChat(chatId)
                } else emptyFlow()
            }
            .combine(sessionStorage.observeAuthInfo()) { messages, authInfo ->
                if (authInfo == null) {
                    return@combine messages
                }

                _state.update {
                    it.copy(
                        messages = messages.map { it.toUi(authInfo.user.id) }
                    )
                }

                messages
            }

        val isNearBottom = state.map { it.isNearBottom }.distinctUntilChanged()
        combine(
            currentMessages,
            newMessages,
            isNearBottom
        ) { currentMessages, newMessages, isNearBottom ->
            val lastNewId = newMessages.lastOrNull()?.message?.id
            val lastCurrentId = currentMessages.lastOrNull()?.id
            if (lastNewId != lastCurrentId && isNearBottom) {
                eventChannel.send(ChatDetailEvent.OnNewMessage)
            }
        }.launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        connectionClient
            .connectionState
            .onEach { connectionState ->
                if (connectionState == ConnectionState.CONNECTED) {
                    _chatId.value?.let {
                        messageRepository.fetchMessages(
                            chatId = it,
                            before = null
                        )
                    }
                }
                _state.update {
                    it.copy(
                        connectionState = connectionState
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onLeaveChatClick() {
        val chatId = _chatId.value ?: return
        _state.update { it.copy(isChatOptionsOpen = false) }

        viewModelScope.launch {
            chatRepository
                .leaveChat(chatId)
                .onSuccess {
                    _state.value.messageTextFieldState.clearText()
                    _chatId.update { null }
                    _state.update {
                        it.copy(
                            chatUi = null,
                            messages = emptyList(),
                            bannerState = BannerState()
                        )
                    }
                }
                .onFailure { error ->
                    eventChannel.send(ChatDetailEvent.OnError(error.toUiText()))
                }
        }
    }

    private fun onDismissChatOptionsClick() {
        _state.update {
            it.copy(
                isChatOptionsOpen = false
            )
        }
    }

    private fun onChatOptionsClick() {
        _state.update {
            it.copy(
                isChatOptionsOpen = true
            )
        }
    }

    private fun switchChat(chatId: String?) {
        _chatId.update { chatId }
        viewModelScope.launch {
            chatId?.let {
                chatRepository.fetchChatById(chatId)
            }
        }
    }
}