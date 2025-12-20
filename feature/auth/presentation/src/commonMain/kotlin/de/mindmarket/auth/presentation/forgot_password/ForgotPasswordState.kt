package de.mindmarket.auth.presentation.forgot_password

import androidx.compose.foundation.text.input.TextFieldState
import de.mindmarket.core.presentation.util.UiText

data class ForgotPasswordState(
    val emailTextFieldState: TextFieldState = TextFieldState(),
    val isLoading: Boolean = false,
    val errorText: UiText? = null,
    val canSubmit: Boolean = false,
    val isEmailSentSuccessfully: Boolean = false
)
