package de.mindmarket.chirp

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true
)
