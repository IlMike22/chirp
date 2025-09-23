package de.mindmarket.chirp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform