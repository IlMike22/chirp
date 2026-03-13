package de.mindmarket.chat.domain.error

import de.mindmarket.core.domain.util.Error

enum class ConnectionError: Error {
    NOT_CONNECTED,
    MESSAGE_SENT_FAILED
}