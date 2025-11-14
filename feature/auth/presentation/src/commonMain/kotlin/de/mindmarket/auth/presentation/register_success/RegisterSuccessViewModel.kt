package de.mindmarket.auth.presentation.register_success

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterSuccessViewModel(): ViewModel() {
    private val _state = MutableStateFlow(RegisterSuccessState(
        registeredEmail = "test@test.com"
    ))
    val state = _state.asStateFlow()

    fun onAction(action: RegisterSuccessAction) {

    }
}