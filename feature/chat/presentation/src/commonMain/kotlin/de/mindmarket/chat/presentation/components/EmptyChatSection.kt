package de.mindmarket.chat.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.empty_chat
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import de.mindmarket.core.presentation.util.DeviceConfiguration
import de.mindmarket.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EmptyListSection(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val configuration = currentDeviceConfiguration()
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.empty_chat),
            contentDescription = title,
            modifier = Modifier.size(
                if (configuration == DeviceConfiguration.MOBILE_LANDSCAPE) 125.dp else 200.dp
            )
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.extended.textPrimary
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.textSecondary
        )
    }
}

@Composable
@Preview
fun EmptyChatSectionPreview() {
    ChirpTheme {
        EmptyListSection(
            title = "title",
            description = "description"
        )
    }
}