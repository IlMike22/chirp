package de.mindmarket.auth.presentation.reset_password

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.error_reset_password_link_expired_or_not_valid
import chirp.feature.auth.presentation.generated.resources.error_reset_password_used_same_password_again
import de.mindmarket.core.domain.auth.AuthService
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.onFailure
import de.mindmarket.core.domain.util.onSuccess
import de.mindmarket.core.domain.validation.PasswordValidator
import de.mindmarket.core.presentation.util.UiText
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

class ResetPasswordViewModel(
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private var hasLoadedInitialData = false

    private val token = savedStateHandle.get<String>("token")
        ?: throw IllegalStateException("Password reset token does not exist.")

    private val isPasswordValidFlow = snapshotFlow { state.value.passwordTextState.text.toString() }
        .map { password -> PasswordValidator.validate(password).isValidPassword }
        .distinctUntilChanged()

    private val _state = MutableStateFlow(ResetPasswordState())
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
            initialValue = ResetPasswordState()
        )

    fun onAction(action: ResetPasswordAction) {
        when (action) {
            is ResetPasswordAction.OnSubmitClick -> resetPassword()
            ResetPasswordAction.OnTogglePasswordVisibilityClick -> _state.update {
                it.copy(
                    isPasswordVisible = !_state.value.isPasswordVisible
                )
            }
        }
    }

    private fun observeValidationState() {
        isPasswordValidFlow.onEach { isValidPassword ->
            _state.update {
                it.copy(
                    canSubmit = isValidPassword
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun resetPassword() {
        if (state.value.isLoading || !state.value.canSubmit) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isResetSuccessful = false,
                    errorText = null
                )
            }

            authService.resetPassword(
                newPassword = _state.value.passwordTextState.text.toString(),
                token = token
            )
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isResetSuccessful = true,
                            errorText = null
                        )
                    }
                }
                .onFailure { error ->
                    val errorText = when (error) {
                        DataError.Remote.UNAUTHORIZED -> UiText.Resource(Res.string.error_reset_password_link_expired_or_not_valid)
                        DataError.Remote.CONFLICT -> UiText.Resource(Res.string.error_reset_password_used_same_password_again)
                        else -> error.toUiText()
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorText = errorText
                        )
                    }
                }
        }
    }
}