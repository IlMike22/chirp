package de.mindmarket.chat.domain.participant

import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result

interface ChatParticipantRepository {
    suspend fun fetchLocalParticipant(): Result<ChatParticipant, DataError>
}