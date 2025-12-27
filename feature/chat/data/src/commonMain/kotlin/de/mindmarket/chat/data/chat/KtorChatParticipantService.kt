package de.mindmarket.chat.data.chat

import de.mindmarket.chat.data.dto.ChatParticipantDto
import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.domain.chat.ChatParticipantService
import de.mindmarket.chat.domain.models.ChatParticipant
import de.mindmarket.core.data.networking.get
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.map
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class KtorChatParticipantService(
    private val httpClient: HttpClient
): ChatParticipantService {
    override suspend fun searchParticipant(query: String): Result<ChatParticipant, DataError.Remote> {
       return httpClient.get<ChatParticipantDto>(
           route = "/participants",
           queryParams = mapOf("query" to query)
       ).map { it.toDomain() }
    }
}