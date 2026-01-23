package de.mindmarket.chat.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.mindmarket.chat.database.dao.ChatDao
import de.mindmarket.chat.database.dao.ChatMessageDao
import de.mindmarket.chat.database.dao.ChatParticipantDao
import de.mindmarket.chat.database.dao.ChatParticipantsCrossRefDao
import de.mindmarket.chat.database.entities.ChatEntity
import de.mindmarket.chat.database.entities.ChatMessageEntity
import de.mindmarket.chat.database.entities.ChatParticipantCrossRef
import de.mindmarket.chat.database.entities.ChatParticipantEntity

@Database(
    entities = [
        ChatEntity::class,
        ChatParticipantEntity::class,
        ChatMessageEntity::class,
        ChatParticipantCrossRef::class,
    ],
    views = [
        LastMessageView::class
    ],
    version = 1
)
abstract class ChirpChatDatabase: RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val chatParticipantDao: ChatParticipantDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatParticipantsCrossRefDao: ChatParticipantsCrossRefDao

    companion object {
        const val DB_NAME = "chirp.db"
    }
}