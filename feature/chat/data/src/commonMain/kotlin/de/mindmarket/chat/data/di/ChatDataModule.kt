package de.mindmarket.chat.data.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import de.mindmarket.chat.data.chat.KtorChatParticipantService
import de.mindmarket.chat.data.chat.KtorChatService
import de.mindmarket.chat.data.chat.OfflineFirstChatRepository
import de.mindmarket.chat.database.DatabaseFactory
import de.mindmarket.chat.domain.chat.ChatParticipantService
import de.mindmarket.chat.domain.chat.ChatRepository
import de.mindmarket.chat.domain.chat.ChatService
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformChatDataModule: Module

val chatDataModule = module {
    includes(platformChatDataModule)
    singleOf(::KtorChatParticipantService) bind ChatParticipantService::class
    singleOf(::KtorChatService) bind ChatService::class
    singleOf(::OfflineFirstChatRepository) bind ChatRepository::class

    single {
        get<DatabaseFactory>()
            .create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}