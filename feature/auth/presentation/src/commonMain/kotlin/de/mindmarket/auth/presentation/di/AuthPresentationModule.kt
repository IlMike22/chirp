package de.mindmarket.auth.presentation.di

import de.mindmarket.auth.presentation.email_verification.EmailVerificationViewModel
import de.mindmarket.auth.presentation.register.RegisterViewModel
import de.mindmarket.auth.presentation.register_success.RegisterSuccessViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
}