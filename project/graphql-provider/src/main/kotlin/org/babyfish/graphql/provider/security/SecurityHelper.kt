package org.babyfish.graphql.provider.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.context.SecurityContext
import reactor.core.publisher.Mono

internal fun <T> executeWithSecurityContext(
    securityContext: SecurityContext?,
    block: suspend () -> T
): Mono<T> =
    mono(Dispatchers.Unconfined) {
        if (securityContext === null) {
            block()
        } else {
            withSecurityContext(securityContext) {
                block()
            }
        }
    }

