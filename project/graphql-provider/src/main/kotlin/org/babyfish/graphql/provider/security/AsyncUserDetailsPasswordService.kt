package org.babyfish.graphql.provider.security

import org.springframework.security.core.userdetails.UserDetails

interface AsyncUserDetailsPasswordService<UD: UserDetails> {

    suspend fun updatePassword(user: UD, newPassword: String): UD
}