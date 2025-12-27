package de.mindmarket.chat.data.di

import de.mindmarket.chat.data.chat.KtorChatParticipantService
import de.mindmarket.chat.data.chat.KtorChatService
import de.mindmarket.chat.domain.chat.ChatParticipantService
import de.mindmarket.chat.domain.chat.ChatService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatDataModule = module {
    singleOf(::KtorChatParticipantService) bind ChatParticipantService::class
    singleOf(::KtorChatService) bind ChatService::class
}