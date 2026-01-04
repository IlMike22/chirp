package de.mindmarket.chat.presentation.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.chat.presentation.chat_detail.components.ChatDetailAction
import de.mindmarket.chat.presentation.chat_detail.components.ChatDetailHeader
import de.mindmarket.chat.presentation.chat_detail.components.MessageBox
import de.mindmarket.chat.presentation.chat_detail.components.MessageList
import de.mindmarket.chat.presentation.components.ChatHeader
import de.mindmarket.chat.presentation.model.ChatUi
import de.mindmarket.chat.presentation.model.MessageUi
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import de.mindmarket.core.presentation.util.UiText
import de.mindmarket.core.presentation.util.clearFocusOnTap
import de.mindmarket.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatDetailRoot(
    chatId:String?,
    isDetailPresent: Boolean,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(chatId) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(chatId))
    }

    BackHandler(
        enabled = !isDetailPresent
    ) {
        viewModel.onAction(ChatDetailAction.OnSelectChat(null))
        onBack()
    }

    ChatDetailScreen(
        state = state,
        isDetailPresent = isDetailPresent,
        onAction = viewModel::onAction
    )
}

@Composable
fun ChatDetailScreen(
    state: ChatDetailState,
    isDetailPresent: Boolean,
    onAction: (ChatDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = currentDeviceConfiguration()
    val messageListState = rememberLazyListState()

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = if (!configuration.isWideScreen) MaterialTheme.colorScheme.surface else
            MaterialTheme.colorScheme.extended.surfaceLower
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .clearFocusOnTap()
                .padding(innerPadding)
                .then(
                    if (configuration.isWideScreen) Modifier.padding(horizontal = 8.dp) else Modifier
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DynamicRoundedCornerColumn(
                    isCornersRounded = configuration.isWideScreen,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    ChatHeader {
                        ChatDetailHeader(
                            chatUi = state.chatUi,
                            isDetailPresent = isDetailPresent,
                            isChatOptionsDropDownOpen = state.isChatOptionsOpen,
                            onChatOptionsClick = {
                                onAction(ChatDetailAction.OnChatOptionsClick)
                            },
                            onDismissChatOptions = {
                                onAction(ChatDetailAction.OnDismissChatOptionsClick)
                            },
                            onLeaveChatClick = {
                                onAction(ChatDetailAction.OnLeaveChatClick)
                            },
                            onManageChatClick = {
                                onAction(ChatDetailAction.OnChatMembersClick)
                            },
                            onBackClick = {
                                onAction(ChatDetailAction.OnBackClick)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    MessageList(
                        messages = state.messages,
                        listState = messageListState,
                        onMessageLongClick = { message ->
                            onAction(ChatDetailAction.OnMessageLongClick(message))
                        },
                        onMessageRetryClick = { message ->
                            onAction(ChatDetailAction.OnRetryClick(message))
                        },
                        onDismissMessageMenu = {
                            onAction(ChatDetailAction.OnDismissMessageMenu)
                        },
                        onDeleteMessageClick = { message ->
                            onAction(ChatDetailAction.OnDeleteMessageClick(message))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    AnimatedVisibility(
                        visible = configuration.isWideScreen.not() && state.chatUi != null
                    ) {
                        MessageBox(
                            messageTextFieldState = state.messageTextFieldState,
                            isTextInputEnabled = state.canSendMessage,
                            connectionState = state.connectionState,
                            onSendClick = {
                                onAction(ChatDetailAction.OnSendMessageClick)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }

                if (configuration.isWideScreen) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                AnimatedVisibility(
                    visible = configuration.isWideScreen && state.chatUi != null
                ) {
                    MessageBox(
                        messageTextFieldState = state.messageTextFieldState,
                        isTextInputEnabled = state.canSendMessage,
                        connectionState = state.connectionState,
                        onSendClick = {
                            onAction(ChatDetailAction.OnSendMessageClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicRoundedCornerColumn(
    isCornersRounded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = if (isCornersRounded) 4.dp else 0.dp,
                shape = if (isCornersRounded) RoundedCornerShape(16.dp) else RectangleShape
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = if (isCornersRounded) RoundedCornerShape(16.dp) else RectangleShape
            ),
    ) {
        content()
    }
}

@Composable
@Preview
fun ChatDetailEmptyPreview() {
    ChirpTheme {
        ChatDetailScreen(
            state = ChatDetailState(),
            isDetailPresent = false,
            onAction = {}
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
@Preview
fun ChatDetailMessagesPreview() {
    ChirpTheme(darkTheme = true) {
        ChatDetailScreen(
            state = ChatDetailState(
                messageTextFieldState = rememberTextFieldState(
                    "This is a new message"
                ),
                canSendMessage = true,
                chatUi = ChatUi(
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

                ),
                messages = (1..20).map {
                    val showLocalMessage = Random.nextBoolean()
                    if (showLocalMessage) {
                        MessageUi.LocalUserMessage(
                            id = Uuid.random().toString(),
                            content = "Hello world.",
                            isMenuOpen = false,
                            deliveryStatus = ChatMessageDeliveryStatus.SENT,
                            formattedSentTime = UiText.DynamicString(
                                "Monday, Aug 20"
                            )
                        )
                    } else {
                        MessageUi.OtherUserMessage(
                            id =  Uuid.random().toString(),
                            content = "Hello world.",
                            formattedSentTime = UiText.DynamicString(
                                "Monday, Aug 20"
                            ),
                            sender = ChatParticipantUi(
                                id = Uuid.random().toString(),
                                initials = "HE",
                                username = "Heike"
                            )
                        )
                    }
                }
            ),
            isDetailPresent = true,
            onAction = {}
        )
    }
}