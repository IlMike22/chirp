package de.mindmarket.auth.presentation.register_success

interface RegisterSuccessAction {
    data object OnLoginClick : RegisterSuccessAction
    data object OnResendVerificationEmailClick : RegisterSuccessAction
}