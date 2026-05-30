package de.mindmarket.chat.domain.participant

import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result

interface ChatParticipantService {
    suspend fun searchParticipant(
        query:String
    ): Result<ChatParticipant, DataError.Remote>

    suspend fun getLocalParticipant(
    ): Result<ChatParticipant, DataError.Remote>
}