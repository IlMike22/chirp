package de.mindmarket.chat.data.participant

import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.chat.domain.participant.ChatParticipantRepository
import de.mindmarket.chat.domain.participant.ChatParticipantService
import de.mindmarket.core.domain.auth.SessionStorage
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.onSuccess
import kotlinx.coroutines.flow.first

class OfflineFirstChatParticipantRepository(
    private val sessionStorage: SessionStorage,
    private val chatParticipantService: ChatParticipantService
) : ChatParticipantRepository {
    override suspend fun fetchLocalParticipant(): Result<ChatParticipant, DataError> {
        return chatParticipantService
            .getLocalParticipant()
            .onSuccess { participant ->
                val currentAuthInfo = sessionStorage.observeAuthInfo().first()
                sessionStorage.set(
                    currentAuthInfo?.copy(
                        user = currentAuthInfo.user.copy(
                            id = participant.userId,
                            username = participant.username,
                            profilePictureUrl = participant.profilePictureUrl
                        )
                    )
                )
            }
    }
}