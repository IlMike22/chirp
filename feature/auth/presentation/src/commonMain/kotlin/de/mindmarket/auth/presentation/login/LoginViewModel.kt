package de.mindmarket.auth.presentation.register

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.error_account_exists
import chirp.feature.auth.presentation.generated.resources.error_invalid_email
import chirp.feature.auth.presentation.generated.resources.error_invalid_password
import chirp.feature.auth.presentation.generated.resources.error_invalid_username
import de.mindmarket.auth.domain.EmailValidator
import de.mindmarket.auth.presentation.login.LoginAction
import de.mindmarket.auth.presentation.login.LoginState
import de.mindmarket.core.domain.auth.AuthService
import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.onFailure
import de.mindmarket.core.domain.util.onSuccess
import de.mindmarket.core.domain.validation.PasswordValidator
import de.mindmarket.core.presentation.util.UiText
import de.mindmarket.core.presentation.util.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authService: AuthService
) : ViewModel() {
    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            observeValidationStates()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LoginState()
        )

    private val isEmailValidFlow = snapshotFlow { state.value.emailTextFieldState.text.toString() }
        .map { email -> EmailValidator.validate(email) }
        .distinctUntilChanged()
    private val isPasswordValidFlow = snapshotFlow { state.value.passwordTextFieldState.text.toString() }
        .map { password -> PasswordValidator.validate(password).isValidPassword }
        .distinctUntilChanged()

    private val isRegisteringFlow = state
        .map { it.isLoggingIn }
        .distinctUntilChanged()

    private fun observeValidationStates() {
        combine(
            isEmailValidFlow,
            isPasswordValidFlow,
            isRegisteringFlow
        ) { isEmailValid, isPasswordValid, isRegistering ->
            _state.update {
                it.copy(
                    canLogin = !isRegistering
                            && isEmailValid
                            && isPasswordValid
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnLoginClick -> login()
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update {
                    it.copy(
                        isPasswordVisible = !it.isPasswordVisible
                    )
                }
            }
            else -> Unit
        }
    }

    private fun login() {
        if (!validateFormInputs()) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoggingIn = true
                )
            }
            val email = state.value.emailTextFieldState.text.toString()
            val password = state.value.passwordTextFieldState.text.toString()

//            authService
//                .login(
//                    email = email,
//                    password = password
//                )
//                .onSuccess {
//                    _state.update {
//                        it.copy(
//                            isLoggingIn = false
//                        )
//                    }
//                    eventChannel.send(RegisterEvent.Success(email))
//                }
//                .onFailure { error ->
//                    val registrationError = when (error) {
//                        DataError.Remote.CONFLICT -> UiText.Resource(Res.string.error_account_exists)
//                        else -> error.toUiText()
//                    }
//                    _state.update {
//                        it.copy(
//                            isLoggingIn = false,
//                            error = registrationError
//                        )
//                    }
//                }
        }
    }

    private fun clearAllTextFieldErrors() {
        _state.update {
            it.copy(
                error = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        clearAllTextFieldErrors()

        val currentState = state.value
        val email = currentState.emailTextFieldState.text.toString()
        val password = currentState.passwordTextFieldState.text.toString()

        val isEmailValid = EmailValidator.validate(email)
        val passwordValidationState = PasswordValidator.validate(password)

        val emailError = if (!isEmailValid) {
            UiText.Resource(Res.string.error_invalid_email)
        } else null

        val passwordError = if (!passwordValidationState.isValidPassword) {
            UiText.Resource(Res.string.error_invalid_password)
        } else null

        _state.update {
            it.copy(
                error = emailError
            )
        }

        return isEmailValid && passwordValidationState.isValidPassword
    }
}