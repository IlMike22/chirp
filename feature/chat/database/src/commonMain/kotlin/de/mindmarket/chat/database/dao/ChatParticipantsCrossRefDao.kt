package de.mindmarket.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.mindmarket.chat.database.entities.ChatParticipantCrossRef
import de.mindmarket.chat.database.entities.ChatParticipantEntity

@Dao
interface ChatParticipantsCrossRefDao {
    @Upsert
    suspend fun upsertCrossRefs(crossRefs: List<ChatParticipantCrossRef>)

    @Query("SELECT userId FROM chatparticipantcrossref WHERE chatId =:chatId")
    suspend fun getActiveParticipantIdsByChat(chatId: String): List<String>

    @Query("SELECT userId FROM chatparticipantcrossref WHERE chatId = :chatId")
    suspend fun getAllParticipantIds(chatId: String): List<String>

    @Query(
        """
        UPDATE ChatParticipantCrossRef
        SET isActive = 0
        WHERE chatId = :chatId AND userId IN (:userIds)
    """
    )
    suspend fun markParticipantsAsInactive(chatId: String, userIds: List<String>)

    @Query(
        """
        UPDATE ChatParticipantCrossRef
        SET isActive = 1
        WHERE chatId = :chatId AND userId IN (:userIds)
    """
    )
    suspend fun reactivateParticipants(chatId: String, userIds: List<String>)

    @Transaction
    suspend fun syncParticipants(
        chatId: String,
        participants: List<ChatParticipantEntity>
    ) {
        if (participants.isEmpty()) {
            return
        }
        val serverParticipantIds = participants.map { it.userId }.toSet()
        val allLocalParticipantIds = getAllParticipantIds(chatId).toSet()
        val activeLocalParticipantIds = getActiveParticipantIdsByChat(chatId).toSet()
        val inactiveLocalParticipantIds = allLocalParticipantIds - activeLocalParticipantIds

        val participantsToReactivate = serverParticipantIds.intersect(inactiveLocalParticipantIds)
        val participantsToDeactivate = activeLocalParticipantIds - serverParticipantIds

        reactivateParticipants(chatId, participantsToReactivate.toList())
        markParticipantsAsInactive(chatId, participantsToDeactivate.toList())

        val completelyNewParticipantIds = serverParticipantIds - allLocalParticipantIds
        val newCrossRefs = completelyNewParticipantIds.map { userId ->
            ChatParticipantCrossRef(
                chatId = chatId,
                userId = userId,
                isActive = true
            )
        }

        upsertCrossRefs(newCrossRefs)
    }
}