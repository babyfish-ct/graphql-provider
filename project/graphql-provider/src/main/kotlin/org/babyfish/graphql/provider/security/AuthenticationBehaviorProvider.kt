package org.babyfish.graphql.provider.security

import org.springframework.security.core.userdetails.UserDetails

interface AuthenticationBehaviorProvider<UD: UserDetails> {

    fun recreateUser(userDetails: UD, newPassword: String): UD

    fun validateRawPassword(rawPassword: String) {
        if (rawPassword.length < 6) {
            throw IllegalArgumentException("Raw password is too short")
        }
    }

    suspend fun afterLogin(userDetails: UD, failed: Boolean) {}

    suspend fun insertUser(user: UD)
}