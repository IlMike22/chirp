package de.mindmarket.auth.presentation.forgot_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mindmarket.auth.domain.EmailValidator
import de.mindmarket.core.domain.auth.AuthService
import de.mindmarket.core.domain.util.onFailure
import de.mindmarket.core.domain.util.onSuccess
import de.mindmarket.core.presentation.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authService: AuthService
) : ViewModel() {
    private var hasLoadedInitialData = false

    val isEmailValidFlow = snapshotFlow { _state.value.emailTextFieldState.text.toString() }
        .map { email ->
            EmailValidator.validate(email)
        }
        .distinctUntilChanged()

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeValidationState()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ForgotPasswordState()
        )

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            ForgotPasswordAction.OnSubmitClick -> submitForgotPasswordRequest()
        }
    }

    private fun observeValidationState() {
        isEmailValidFlow.onEach { isEmailValid ->
            _state.update { it.copy(canSubmit = isEmailValid) }
        }
            .launchIn(viewModelScope)
    }

    private fun submitForgotPasswordRequest() {
        if (_state.value.isLoading || !_state.value.canSubmit) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isEmailSentSuccessfully = false,
                    errorText = null
                )
            }

            authService.forgotPassword(
                email = _state.value.emailTextFieldState.text.toString()
            ).onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isEmailSentSuccessfully = true
                    )
                }
            }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorText = error.toUiText()
                        )
                    }
                }
        }
    }
}