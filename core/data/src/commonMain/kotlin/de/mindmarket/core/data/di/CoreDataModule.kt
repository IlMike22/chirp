package de.mindmarket.core.data.di

import de.mindmarket.core.data.auth.DataStoreSessionStorage
import de.mindmarket.core.data.auth.KtorAuthService
import de.mindmarket.core.data.logging.KermitLogger
import de.mindmarket.core.data.networking.HttpClientFactory
import de.mindmarket.core.domain.auth.AuthService
import de.mindmarket.core.domain.auth.SessionStorage
import de.mindmarket.core.domain.logging.ChirpLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<ChirpLogger> { KermitLogger }
    single {
        HttpClientFactory(get(), get()).create(get())
    }
    singleOf(::KtorAuthService) bind AuthService::class
    singleOf(::DataStoreSessionStorage) bind SessionStorage::class
}