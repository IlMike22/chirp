package de.mindmarket.chat.presentation.chat_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.create_chat
import chirp.feature.chat.presentation.generated.resources.do_you_want_to_logout
import chirp.feature.chat.presentation.generated.resources.do_you_want_to_logout_description
import chirp.feature.chat.presentation.generated.resources.logout
import chirp.feature.chat.presentation.generated.resources.no_chats
import chirp.feature.chat.presentation.generated.resources.no_chats_subtitle
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.presentation.chat_list.components.ChatListHeader
import de.mindmarket.chat.presentation.chat_list.components.ChatListItemUi
import de.mindmarket.chat.presentation.components.EmptyListSection
import de.mindmarket.chat.presentation.model.ChatUi
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi
import de.mindmarket.core.designsystem.components.brand.ChirpHorizontalDivider
import de.mindmarket.core.designsystem.components.buttons.ChirpFloatingActionButton
import de.mindmarket.core.designsystem.components.dialogs.DestructiveConfirmationDialog
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun ChatListScreenRoot(
    onChatClick: (ChatUi) -> Unit,
    onConfirmLogoutClick: () -> Unit,
    onCreateChatClick: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ChatListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is ChatListAction.OnChatClick -> onChatClick(action.chat)
                ChatListAction.OnConfirmLogoutClick -> onConfirmLogoutClick()
                ChatListAction.OnCreateChatClick -> onCreateChatClick()
                ChatListAction.OnProfileSettingsClick -> onProfileSettingsClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        floatingActionButton = {
            ChirpFloatingActionButton(
                onClick = {
                    onAction(ChatListAction.OnCreateChatClick)
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.create_chat)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatListHeader(
                localParticipant = state.localParticipant,
                isUserMenuOpen = state.isUserMenuOpen,
                onUserAvatarClick = { onAction(ChatListAction.OnUserAvatarClick) },
                onLogoutClick = { onAction(ChatListAction.OnLogoutClick) },
                onDismissMenu = { onAction(ChatListAction.OnDismissUserMenu) },
                onProfileSettingsClick = { onAction(ChatListAction.OnProfileSettingsClick) }
            )
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                state.chats.isEmpty() -> {
                    EmptyListSection(
                        title = stringResource(Res.string.no_chats),
                        description = stringResource(Res.string.no_chats_subtitle),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(
                            items = state.chats,
                            key = { it.id }
                        ) { chatUi ->
                            ChatListItemUi(
                                chat = chatUi,
                                isSelected = chatUi.id == state.selectedChatId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAction(ChatListAction.OnChatClick(chatUi))
                                    }
                            )
                            ChirpHorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (state.showLogoutConfirmation) {
        DestructiveConfirmationDialog(
            title = stringResource(Res.string.do_you_want_to_logout),
            description = stringResource(Res.string.do_you_want_to_logout_description),
            confirmButtonText = stringResource(Res.string.logout),
            cancelButtonText = stringResource(Res.string.cancel),
            onCancelClick = { onAction(ChatListAction.OnDismissLogoutDialog) },
            onConfirmClick = { onAction(ChatListAction.OnConfirmLogoutClick) },
            onDismiss = { onAction(ChatListAction.OnDismissLogoutDialog) }
        )
    }
}

@Composable
@Preview
fun ChatListScreenPreview() {
    ChirpTheme {
        ChatListScreen(
            state = ChatListState(
                chats = listOf(
                    ChatUi(
                        id = "1",
                        localParticipant = ChatParticipantUi(
                            id = "2",
                            username = "Mike",
                            initials = "MW",
                            imageUrl = null
                        ),
                        otherParticipants = listOf(
                            ChatParticipantUi(
                                id = "2",
                                username = "Uta",
                                initials = "UU",
                                imageUrl = null
                            ),
                            ChatParticipantUi(
                                id = "2",
                                username = "Sabrina",
                                initials = "SA",
                                imageUrl = null
                            )
                        ),
                        lastMessage = ChatMessage(
                            id = "2",
                            chatId = "333",
                            senderId = "Uta",
                            createdAt = Clock.System.now(),
                            content = "This is the last message from Uta. You can see the details of that message by clicking",
                        ),
                        lastMessageSenderUsername = "Uta"

                    )
                )
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}