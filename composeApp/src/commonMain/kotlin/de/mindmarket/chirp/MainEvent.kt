package de.mindmarket.chirp

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}