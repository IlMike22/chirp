package de.mindmarket.chat.data.message

import de.mindmarket.chat.data.dto.ChatMessageDto
import de.mindmarket.chat.data.mappers.toDomain
import de.mindmarket.chat.domain.message.ChatMessageService
import de.mindmarket.chat.domain.models.ChatMessage
import de.mindmarket.core.data.networking.get
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.Result
import de.mindmarket.core.domain.util.map
import io.ktor.client.HttpClient

class KtorChatMessageService(
    private val httpClient: HttpClient
): ChatMessageService {
    override suspend fun fetchMessages(
        chatId: String,
        before: String?
    ): Result<List<ChatMessage>, DataError.Remote> {
        return httpClient.get<List<ChatMessageDto>>(
            route = "/chat/$chatId/messages",
            queryParams = buildMap {
                this["pageSize"] = ChatMessageConstants.PAGE_SIZE
                if (before != null) {
                    this["before"] = before
                }
            }
        ).map { it.map { it.toDomain() } }
    }
}