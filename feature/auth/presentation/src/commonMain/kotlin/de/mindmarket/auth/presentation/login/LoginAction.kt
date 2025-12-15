package de.mindmarket.auth.presentation.login

import de.mindmarket.auth.presentation.register.RegisterAction

sealed interface LoginAction {
    data object OnTogglePasswordVisibility: LoginAction
    data object OnForgotPasswordClick: LoginAction
    data object OnLoginClick: LoginAction
    data object OnSignUpClick: LoginAction
}