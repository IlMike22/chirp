package de.mindmarket.core.data.mappers

import de.mindmarket.core.data.dto.AuthInfoSerializable
import de.mindmarket.core.data.dto.UserSerializable
import de.mindmarket.core.domain.auth.AuthInfo
import de.mindmarket.core.domain.auth.User

fun AuthInfoSerializable.toDomain(): AuthInfo {
    return AuthInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun UserSerializable.toDomain(): User {
    return User(
        id = this.id,
        email = this.email,
        username = this.username,
        hasVerifiedEmail = this.hasVerifiedEmail,
        profilePictureUrl = this.profilePictureUrl
    )
}