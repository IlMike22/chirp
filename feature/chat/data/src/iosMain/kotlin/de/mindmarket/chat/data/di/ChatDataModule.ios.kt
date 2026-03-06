package de.mindmarket.chat.data.di

import de.mindmarket.chat.data.lifecycle.AppLifecycleObserver
import de.mindmarket.chat.data.network.ConnectivityObserver
import de.mindmarket.chat.database.DatabaseFactory
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformChatDataModule = module {
    single { DatabaseFactory() }
    singleOf(::AppLifecycleObserver)
    singleOf(::ConnectivityObserver)
}
