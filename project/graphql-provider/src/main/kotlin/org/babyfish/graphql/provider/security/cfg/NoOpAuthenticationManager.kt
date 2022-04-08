package org.babyfish.graphql.provider.security.cfg

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@ConditionalOnMissingBean(ReactiveAuthenticationManager::class)
open class NoOpAuthenticationManager : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> =
        if (authentication is UsernamePasswordAuthenticationToken) {
            throw IllegalStateException(
                "The authentication is specified by the client, " +
                    "please support an implementation of '${ReactiveAuthenticationManager::class.qualifiedName}'"
            )
        } else {
            Mono.empty()
        }
}