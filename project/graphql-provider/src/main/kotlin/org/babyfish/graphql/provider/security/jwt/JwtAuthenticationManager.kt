package org.babyfish.graphql.provider.security.jwt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.AuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
internal open class JwtAuthenticationManager(
    private val properties: GraphQLProviderProperties,
    private val userDetailsService: ReactiveUserDetailsService
): ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> =
        mono(Dispatchers.Unconfined) {
            authenticateAsync(authentication)
        }

    private suspend fun authenticateAsync(authentication: Authentication): Authentication? =
        if (authentication is JwtToken) {
            val anonymous = properties.security.anonymous
            if (authentication.principal == "") {
                AuthenticationToken(
                    anonymous.principal,
                    anonymous.roles.map { SimpleGrantedAuthority(it) }
                )
            } else {
                if (authentication.hasDeserializedAuthorities) {
                    AuthenticationToken(authentication.principal, authentication.authorities)
                } else {
                    val user = findByUsername(authentication.principal)
                        ?: throw IllegalArgumentException("Illegal principal: '${authentication.principal}'")
                    AuthenticationToken(user, user.authorities)
                }
            }
        } else {
            null
        }

    private suspend fun findByUsername(userName: String): UserDetails? =
        userDetailsService.findByUsername(userName).awaitSingleOrNull()?.also {
            validateUser(it)
        }

    private fun validateUser(user: UserDetails) {
        if (!user.isAccountNonLocked) {
            logger.debug("User account is locked")
            throw LockedException("User account is locked")
        }
        if (!user.isEnabled) {
            logger.debug("User account is disabled")
            throw DisabledException("User is disabled")
        }
        if (!user.isAccountNonExpired) {
            logger.debug("User account is expired")
            throw AccountExpiredException("User account has expired")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthenticationManager::class.java)
    }
}