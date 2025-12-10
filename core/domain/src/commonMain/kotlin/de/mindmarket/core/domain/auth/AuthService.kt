package de.mindmarket.core.domain.auth

import de.mindmarket.core.domain.util.DataError
import de.mindmarket.core.domain.util.EmptyResult

interface AuthService {
    suspend fun register(
        email: String,
        username: String,
        password: String
    ): EmptyResult<DataError.Remote>

    suspend fun resendVerificationEmail(
        email: String
    ): EmptyResult<DataError.Remote>

    suspend fun verifyEmail(token:String): EmptyResult<DataError.Remote>
}