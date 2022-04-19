package org.babyfish.graphql.provider.security.jwt

import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
internal class JwtTokenConverter(
    private val properties: GraphQLProviderProperties,
    private val jwtTokenService: JwtTokenService
) : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val anonymous = properties.security.anonymous
        val header = exchange
            .request
            .headers
            .getFirst(properties.security.jwt.header)
            ?.takeIf { it.isNotEmpty() }
        return if (header === null) {
            Mono.just(
                JwtToken("", anonymous.roles.map { SimpleGrantedAuthority(it) })
            )
        } else if (header.startsWith(BEARER_PREFIX)) {
            Mono.just(
                jwtTokenService.fromTokenString(header.substring(BEARER_PREFIX.length))
            )
        } else {
            Mono.just(
                jwtTokenService.fromTokenString(header)
            )
        }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}