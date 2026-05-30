package de.mindmarket.chat.data.participant

import de.mindmarket.chat.data.dto.ChatParticipantDto
import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.domain.participant.ChatParticipantService
import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.data.networking.get
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatParticipantService(
    private val httpClient: HttpClient
): ChatParticipantService {
    override suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote> {
       return httpClient.get<ChatParticipantDto>(
           route = "/participants",
           queryParams = mapOf("query" to query)
       ).map { it.toDomain() }
    }

    override suspend fun getLocalParticipant(): Result<ChatParticipant, DataError.Remote> {
        return httpClient.get<ChatParticipantDto>(
            route = "/participants",
        ).map { it.toDomain() }
    }
}