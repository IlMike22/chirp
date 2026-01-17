package de.mindmarket.chat.data.di

import de.mindmarket.chat.database.DatabaseFactory
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
}
