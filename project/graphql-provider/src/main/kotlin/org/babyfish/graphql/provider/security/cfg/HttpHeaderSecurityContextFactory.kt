package org.babyfish.graphql.provider.security.cfg

import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Repository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Repository
open class HttpHeaderSecurityContextFactory(
    private val authenticationManager: ReactiveAuthenticationManager,
    private val properties: GraphQLProviderProperties
): ServerSecurityContextRepository {

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> =
        Mono.empty()

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        return authenticate(exchange).map {
            SecurityContextImpl(it).apply {
                exchange.attributes[SECURITY_CONTEXT_ATTR_KEY] = this
            }
        }
    }

    private fun authenticate(exchange: ServerWebExchange): Mono<Authentication> {
        val token = exchange.request.headers.getFirst(properties.security.httpHeader)?.let {
            if (it.startsWith(BEARER_PREFIX)) {
                it.substring(BEARER_PREFIX.length)
            } else {
                it
            }
        }?.takeIf {
            it.isNotEmpty()
        } ?: return if (properties.security.anonymous) {
            Mono.just(
                UsernamePasswordAuthenticationToken(null, null, emptyList())
            )
        } else {
            Mono.empty()
        }
        return authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(token, token)
        )
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        const val SECURITY_CONTEXT_ATTR_KEY = "SPRING_SECURITY_CONTEXT"
    }
}