package org.babyfish.graphql.provider.security

import org.springframework.security.core.userdetails.UserDetails

interface AsyncUserDetailsService<UD: UserDetails> {

    suspend fun findByUsername(name: String): UD?
}