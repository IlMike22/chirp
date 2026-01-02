package de.mindmarket.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.arrow_left_icon
import chirp.core.designsystem.generated.resources.dots_icon
import chirp.core.designsystem.generated.resources.log_out_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.chat_members
import chirp.feature.chat.presentation.generated.resources.go_back
import chirp.feature.chat.presentation.generated.resources.leave_chat
import chirp.feature.chat.presentation.generated.resources.open_chat_options_menu
import chirp.feature.chat.presentation.generated.resources.users_icon
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.chat.presentation.components.ChatHeader
import de.mindmarket.chat.presentation.components.ChatItemHeaderRow
import de.mindmarket.chat.presentation.model.ChatUi
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi
import de.mindmarket.core.designsystem.components.buttons.ChirpIconButton
import de.mindmarket.core.designsystem.components.dropdown.ChirpDropDownMenu
import de.mindmarket.core.designsystem.components.dropdown.DropDownItem
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Instant
import chirp.core.designsystem.generated.resources.Res as DesignRes

@Composable
fun ChatDetailHeader(
    chatUi: ChatUi,
    isDetailPresent: Boolean,
    isChatOptionsDropDownOpen: Boolean,
    onChatOptionsClick: () -> Unit,
    onDismissChatOptions: () -> Unit,
    onManageChatClick: () -> Unit,
    onLeaveChatClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGroupChat = chatUi.otherParticipants.size > 1
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isDetailPresent.not()) {
            ChirpIconButton(
                onClick = onBackClick,
                content = {
                    Icon(
                        imageVector = vectorResource(DesignRes.drawable.arrow_left_icon),
                        contentDescription = stringResource(Res.string.go_back),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.extended.textSecondary
                    )
                }
            )
        }

        ChatItemHeaderRow(
            chat = chatUi,
            isGroupChat = isGroupChat,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onManageChatClick()
                }
        )

        Box {
            ChirpIconButton(
                onClick = onChatOptionsClick
            ) {
                Icon(
                    imageVector = vectorResource(DesignRes.drawable.dots_icon),
                    contentDescription = stringResource(Res.string.open_chat_options_menu),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.extended.textSecondary
                )
            }

            ChirpDropDownMenu(
                isOpen = isChatOptionsDropDownOpen,
                onDismiss = onDismissChatOptions,
                items = listOf(
                    DropDownItem(
                        title = stringResource(Res.string.chat_members),
                        icon = vectorResource(Res.drawable.users_icon),
                        contentColor = MaterialTheme.colorScheme.extended.textSecondary,
                        onClick = onManageChatClick
                    ),
                    DropDownItem(
                        title = stringResource(Res.string.leave_chat),
                        icon = vectorResource(DesignRes.drawable.log_out_icon),
                        contentColor = MaterialTheme.colorScheme.extended.destructiveHover,
                        onClick = onLeaveChatClick
                    )
                )
            )
        }
    }
}

@Composable
@Preview
fun ChatListDetailHeaderUiPreview() {
    ChirpTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ChatHeader {
                ChatDetailHeader(
                    chatUi = ChatUi(
                        id = "2",
                        localParticipant = ChatParticipantUi(
                            id = "1",
                            username = "Mike",
                            initials = "MW",
                            imageUrl = null
                        ),
                        otherParticipants = listOf(
                            ChatParticipantUi("2", "Matthias", "MM"),
                            ChatParticipantUi("3", "Sabrina", "SO"),
                            ChatParticipantUi("4", "Klothilde", "KW")
                        ),
                        lastMessage = ChatMessage(
                            id = "2",
                            chatId = "1",
                            content = "This is the last message I send you. It goes over multiple lines to show the Ellupses",
                            createdAt = Clock.System.now(),
                            senderId = "2"
                        ),
                        lastMessageSenderUsername = "Klothilde"),
                    isDetailPresent = false,
                    isChatOptionsDropDownOpen = true,
                    onBackClick = {},
                    onManageChatClick = {},
                    onLeaveChatClick = {},
                    onChatOptionsClick = {},
                    onDismissChatOptions = {},
                )

            }
        }
    }
}