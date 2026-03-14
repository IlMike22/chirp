package de.mindmarket.chat.data.message

import de.mindmarket.chat.database.ChirpChatDatabase
import de.mindmarket.chat.domain.message.MessageRepository
import de.mindmarket.chat.domain.models.ChatMessageDeliveryStatus
import de.mindmarket.core.data.database.safeDatabaseUpdate
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult
import kotlin.time.Clock

class OfflineFirstMessageRepository(
    private val database: ChirpChatDatabase
): MessageRepository {
    override suspend fun updateMessageDeliveryStatus(
        messageId: String,
        status: ChatMessageDeliveryStatus
    ): EmptyResult<DataError.Local> {
        return safeDatabaseUpdate {
            database.chatMessageDao.updateDeliveryStatus(
                messageId = messageId,
                status = status.name,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
}