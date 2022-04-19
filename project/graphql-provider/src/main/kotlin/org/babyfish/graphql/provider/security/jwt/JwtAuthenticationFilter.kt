package org.babyfish.graphql.provider.security.jwt

import org.babyfish.graphql.provider.security.cfg.AuthenticatedPatternSupplier
import org.babyfish.graphql.provider.security.cfg.AuthenticationEntryPoint
import org.babyfish.graphql.provider.security.cfg.SecurityConfiguration
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
internal class JwtAuthenticationFilter(
    jwtTokenConverter: JwtTokenConverter,
    jwtAuthenticationManager: JwtAuthenticationManager,
    authenticatedPatternSupplier: AuthenticatedPatternSupplier?,
) : AuthenticationWebFilter(
    jwtAuthenticationManager
) {
    init {
        setServerAuthenticationConverter(jwtTokenConverter)
        setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers(
                *(authenticatedPatternSupplier?.patterns() ?: arrayOf("/graphql"))
            )
        )
        setAuthenticationFailureHandler(
            ServerAuthenticationEntryPointFailureHandler(
                AuthenticationEntryPoint()
            )
        )
    }

    override fun onAuthenticationSuccess(
        authentication: Authentication,
        webFilterExchange: WebFilterExchange
    ): Mono<Void> {
        webFilterExchange.exchange.attributes[SecurityConfiguration.GRAPHQL_AUTHENTICATION_KEY] = authentication
        return super.onAuthenticationSuccess(authentication, webFilterExchange)
    }
}