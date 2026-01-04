package de.mindmarket.chat.presentation.chat_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.mindmarket.chat.presentation.model.MessageUi
import de.mindmarket.core.designsystem.components.avatar.ChirpAvatarPhoto
import de.mindmarket.core.designsystem.components.chat.ChirpChatBubble
import de.mindmarket.core.designsystem.components.chat.TrianglePosition

@Composable
fun OtherUserMessage(
    message: MessageUi.OtherUserMessage,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChirpAvatarPhoto(
            displayText = message.sender.initials,
            imageUrl = message.sender.imageUrl
        )

        ChirpChatBubble(
            messageContent = message.content,
            sender = message.sender.username,
            formattedDateTime = message.formattedSentTime.asString(),
            color = color,
            trianglePosition = TrianglePosition.LEFT
        )
    }
}