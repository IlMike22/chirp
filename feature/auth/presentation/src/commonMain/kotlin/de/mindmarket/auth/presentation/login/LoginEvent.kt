package de.mindmarket.auth.presentation.login

import de.mindmarket.auth.presentation.register.RegisterEvent

sealed interface LoginEvent {
    data class Success(val email: String): LoginEvent
}