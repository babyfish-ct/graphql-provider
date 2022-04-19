package org.babyfish.graphql.provider.security.jwt

import org.springframework.security.core.userdetails.UserDetails

interface JwtAuthenticationService {

    suspend fun login(
        username: String,
        password: String
    ): JwtAuthenticationResult

    suspend fun encryptedUser(
        user: UserDetails
    ): UserDetails

    suspend fun createUser(
        user: UserDetails,
        isPasswordEncoded: Boolean = false
    ): JwtAuthenticationResult

    suspend fun updatePassword(
        oldPassword: String,
        newPassword: String
    ): JwtAuthenticationResult

    suspend fun refreshAccessToken(
        refreshToken: String
    ): JwtAuthenticationResult
}