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

class RegisterViewModel(
    private val authService: AuthService
) : ViewModel() {
    private val eventChannel = Channel<RegisterEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(RegisterState())
    val state = _state
        .onStart {
            observeValidationStates()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = RegisterState()
        )

    private val isEmailValidFlow = snapshotFlow { state.value.emailTextState.text.toString() }
        .map { email -> EmailValidator.validate(email) }
        .distinctUntilChanged()
    private val isUserNameValidFlow = snapshotFlow { state.value.usernameTextState.text.toString() }
        .map { username -> username.length in 3..20 }
        .distinctUntilChanged()
    private val isPasswordValidFlow = snapshotFlow { state.value.passwordTextState.text.toString() }
        .map { password -> PasswordValidator.validate(password).isValidPassword }
        .distinctUntilChanged()

    private val isRegisteringFlow = state
        .map { it.isRegistering }
        .distinctUntilChanged()

    private fun observeValidationStates() {
        combine(
            isEmailValidFlow,
            isUserNameValidFlow,
            isPasswordValidFlow,
            isRegisteringFlow
        ) { isEmailValid, isUserNameValid, isPasswordValid, isRegistering ->
            _state.update {
                it.copy(
                    canRegister = !isRegistering
                            && isEmailValid
                            && isUserNameValid
                            && isPasswordValid
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: RegisterAction) {
        when (action) {
            RegisterAction.OnLoginClick -> Unit
            RegisterAction.OnRegisterClick -> register()
            RegisterAction.OnTogglePasswordVisibleClick -> {
                _state.update {
                    it.copy(
                        isPasswordVisible = !it.isPasswordVisible
                    )
                }
            }

            else -> Unit
        }
    }

    private fun register() {
        if (!validateFormInputs()) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRegistering = true
                )
            }
            val email = state.value.emailTextState.text.toString()
            val username = state.value.usernameTextState.text.toString()
            val password = state.value.passwordTextState.text.toString()

            authService
                .register(
                    email = email,
                    username = username,
                    password = password
                )
                .onSuccess {
                    _state.update {
                        it.copy(
                            isRegistering = false
                        )
                    }
                    eventChannel.send(RegisterEvent.Success(email))
                }
                .onFailure { error ->
                    val registrationError = when (error) {
                        DataError.Remote.CONFLICT -> UiText.Resource(Res.string.error_account_exists)
                        else -> error.toUiText()
                    }
                    _state.update {
                        it.copy(
                            isRegistering = false,
                            registrationError = registrationError
                        )
                    }
                }
        }
    }

    private fun clearAllTextFieldErrors() {
        _state.update {
            it.copy(
                emailError = null,
                passwordError = null,
                usernameError = null,
                registrationError = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        clearAllTextFieldErrors()

        val currentState = state.value
        val email = currentState.emailTextState.text.toString()
        val username = currentState.usernameTextState.text.toString()
        val password = currentState.passwordTextState.text.toString()

        val isEmailValid = EmailValidator.validate(email)
        val passwordValidationState = PasswordValidator.validate(password)
        val isUsernameValid = username.length in 3..20

        val emailError = if (!isEmailValid) {
            UiText.Resource(Res.string.error_invalid_email)
        } else null

        val usernameError = if (!isUsernameValid) {
            UiText.Resource(Res.string.error_invalid_username)
        } else null

        val passwordError = if (!passwordValidationState.isValidPassword) {
            UiText.Resource(Res.string.error_invalid_password)
        } else null

        _state.update {
            it.copy(
                emailError = emailError,
                usernameError = usernameError,
                passwordError = passwordError
            )
        }

        return isUsernameValid && isEmailValid && passwordValidationState.isValidPassword
    }
}