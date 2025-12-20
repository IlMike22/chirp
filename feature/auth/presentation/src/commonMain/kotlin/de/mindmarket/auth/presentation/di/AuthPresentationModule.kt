package de.mindmarket.auth.presentation.di

import de.mindmarket.auth.presentation.email_verification.EmailVerificationViewModel
import de.mindmarket.auth.presentation.forgot_password.ForgotPasswordViewModel
import de.mindmarket.auth.presentation.login.LoginViewModel
import de.mindmarket.auth.presentation.register.RegisterViewModel
import de.mindmarket.auth.presentation.register_success.RegisterSuccessViewModel
import de.mindmarket.auth.presentation.reset_password.ResetPasswordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}