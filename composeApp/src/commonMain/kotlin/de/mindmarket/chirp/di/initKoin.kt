package de.mindmarket.chirp.di

import de.mindmarket.auth.presentation.di.authPresentationModule
import de.mindmarket.chat.presentation.chat_list.di.chatPresentationModule
import de.mindmarket.core.data.di.coreDataModule
import de.mindmarket.core.presentation.di.corePresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            authPresentationModule,
            appModule,
            chatPresentationModule,
            corePresentationModule
        )
    }
}