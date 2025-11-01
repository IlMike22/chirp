package de.mindmarket.auth.presentation.register

sealed interface RegisterAction {
    data object OnLoginClick : RegisterAction
    data object OnInputTextFocusGain : RegisterAction
    data object OnRegisterClick : RegisterAction
    data object OnTogglePasswordVisibleClick : RegisterAction
}