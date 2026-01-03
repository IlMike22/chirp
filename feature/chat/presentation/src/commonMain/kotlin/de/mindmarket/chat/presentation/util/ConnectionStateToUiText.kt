package de.mindmarket.chat.presentation.util

import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.error_network
import chirp.feature.chat.presentation.generated.resources.error_unknown
import chirp.feature.chat.presentation.generated.resources.offline
import chirp.feature.chat.presentation.generated.resources.online
import chirp.feature.chat.presentation.generated.resources.reconnecting
import de.mindmarket.chat.domain.models.ConnectionState
import de.mindmarket.core.presentation.util.UiText

fun ConnectionState.toUiText(): UiText {
    val resource = when (this) {
        ConnectionState.DISCONNECTED -> Res.string.offline
        ConnectionState.CONNECTING -> Res.string.reconnecting
        ConnectionState.CONNECTED -> Res.string.online
        ConnectionState.ERROR_NETWORK -> Res.string.error_network
        ConnectionState.ERROR_UNKNOWN -> Res.string.error_unknown
    }

    return UiText.Resource(resource)
}