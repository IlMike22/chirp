package de.mindmarket.auth.presentation.email_verification

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmailVerificationViewModel(): ViewModel() {
    private val _state = MutableStateFlow(EmailVerificationState())
    val state = _state.asStateFlow()

    fun onAction(action: EmailVerificationAction) {

    }
}