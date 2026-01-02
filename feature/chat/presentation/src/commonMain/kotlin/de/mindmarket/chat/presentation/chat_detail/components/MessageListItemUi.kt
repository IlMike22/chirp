package de.mindmarket.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.chat.presentation.model.MessageUi
import de.mindmarket.core.designsystem.components.avatar.ChatParticipantUi
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import de.mindmarket.core.designsystem.theme.labelXSmall
import de.mindmarket.core.presentation.util.UiText
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MessageListItemUi(
    messageUi: MessageUi,
    onMessageLongClick: () -> Unit,
    onDismissMessageMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        when (messageUi) {
            is MessageUi.DateSeparator -> {
                DateSeparatorUi(
                    date = messageUi.date.asString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            is MessageUi.LocalUserMessage -> {
                LocalUserMessage(
                    message = messageUi,
                    onMessageLongClick = onMessageLongClick,
                    onDeleteClick = onDeleteClick,
                    onDismissMessageMenu = onDismissMessageMenu,
                    onRetryClick = onRetryClick
                )
            }
            is MessageUi.OtherUserMessage -> {
                OtherUserMessage(
                    message = messageUi
                )
            }
        }
    }
}



@Composable
private fun DateSeparatorUi(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier
            .weight(1f))

        Text(
            text = date,
            modifier = Modifier
                .padding(horizontal = 40.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.extended.textPlaceholder
        )

        HorizontalDivider(modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
@Preview
fun MessageListItemUiDateSeparatorPreview() {
    ChirpTheme {
        MessageListItemUi(
            messageUi = MessageUi.DateSeparator(
                id = "2",
                date = UiText.DynamicString("Today")
            ),
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {}
        )
    }
}

@Composable
@Preview
fun MessageListItemLocalUserMessageUiPreview() {
    ChirpTheme {
        MessageListItemUi(
            messageUi = MessageUi.LocalUserMessage(
                id = "2",
                content = "This is some longer content to see if everything looks good.",
                deliveryStatus = ChatMessageDeliveryStatus.FAILED,
                isMenuOpen = true,
                formattedSentTime = UiText.DynamicString("Today")
            ),
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
@Preview
fun MessageListItemOtherUserMessagePreview() {
    ChirpTheme {
        MessageListItemUi(
            messageUi = MessageUi.OtherUserMessage(
                id = "2",
                content = "This is some longer content to see if everything looks good.",
                sender = ChatParticipantUi(
                    id = "1",
                    username = "Mike",
                    initials = "MW"
                ),
                formattedSentTime = UiText.DynamicString("Today")
            ),
            onMessageLongClick = {},
            onDismissMessageMenu = {},
            onDeleteClick = {},
            onRetryClick = {}
        )
    }
}