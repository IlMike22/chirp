package de.mindmarket.chat.presentation.create_chat

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.error_participant_not_found
import de.mindmarket.chat.domain.chat.ChatParticipantService
import de.mindmarket.chat.domain.chat.ChatRepository
import de.mindmarket.chat.presentation.mappers.toUi
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.onFailure
import de.mindmarket.core.domain.util.onSuccess
import de.mindmarket.core.presentation.util.UiText
import de.mindmarket.core.presentation.util.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class CreateChatViewModel(
    private val chatParticipantService: ChatParticipantService,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private var hasLoadedInitialData: Boolean = false

    private val eventChannel = Channel<CreateChatEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(CreateChatState())

    @OptIn(FlowPreview::class)
    private val searchFlow = snapshotFlow { _state.value.queryTextState.text.toString() }
        .debounce { 1.seconds }
        .onEach { query ->
            performSearch(query)
        }

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                searchFlow.launchIn(viewModelScope)
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateChatState()
        )

    fun onAction(action: CreateChatAction) {
        when (action) {
            CreateChatAction.OnAddClick -> addParticipant()
            CreateChatAction.OnCreateChatClick -> createChat()
            else -> Unit
        }
    }

    private fun createChat() {
        val userIds = state.value.selectedChatParticipants.map { it.id }
        if (userIds.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isCreatingChat = true,
                    canAddParticipant = false
                )
            }

            chatRepository
                .createChat(userIds)
                .onSuccess { chat ->
                    _state.update {
                        it.copy(
                            isCreatingChat = false,
                            createChatError = null,
                        )
                    }
                    eventChannel.send(CreateChatEvent.OnChatCreated(chat))
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            createChatError = error.toUiText(),
                            canAddParticipant = it.currentSearchResult != null && !it.isSearching,
                            isCreatingChat = false
                        )
                    }
                }
        }
    }

    private fun addParticipant() {
        state.value.currentSearchResult?.let { participant ->
            val isAlreadyPartOfChat = state.value.selectedChatParticipants.any {
                it.id == participant.id
            }

            if (!isAlreadyPartOfChat) {
                _state.update {
                    it.copy(
                        selectedChatParticipants = it.selectedChatParticipants + participant,
                        canAddParticipant = false,
                        currentSearchResult = null
                    )
                }

                _state.value.queryTextState.clearText()
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    currentSearchResult = null,
                    canAddParticipant = false,
                    searchError = null
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSearching = true,
                    canAddParticipant = false,
                )
            }

            chatParticipantService
                .searchParticipant(query)
                .onSuccess { participant ->
                    _state.update {
                        it.copy(
                            currentSearchResult = participant.toUi(),
                            isSearching = false,
                            canAddParticipant = true,
                            searchError = null
                        )
                    }
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        DataError.Remote.NOT_FOUND -> UiText.Resource(Res.string.error_participant_not_found)
                        else -> error.toUiText()
                    }
                    _state.update {
                        it.copy(
                            isSearching = false,
                            canAddParticipant = false,
                            searchError = errorMessage,
                            currentSearchResult = null
                        )
                    }
                }
        }
    }
}