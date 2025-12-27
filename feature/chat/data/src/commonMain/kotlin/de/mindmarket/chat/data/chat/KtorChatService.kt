package de.mindmarket.chat.data.chat

import de.mindmarket.chat.data.dto.ChatDto
import de.mindmarket.chat.data.dto.request.CreateChatRequest
import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.domain.chat.ChatService
import de.mindmarket.chat.domain.models.Chat
import de.mindmarket.core.data.networking.post
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatService(
    private val httpClient: HttpClient
) : ChatService {
    override suspend fun createChat(otherUserIds: List<String>): Result<Chat, DataError.Remote> {
        return httpClient.post<CreateChatRequest, ChatDto>(
            route = "/chat",
            body = CreateChatRequest(
                otherUserIds = otherUserIds
            )
        ).map { chatDto ->
            chatDto.toDomain()
        }
    }
}