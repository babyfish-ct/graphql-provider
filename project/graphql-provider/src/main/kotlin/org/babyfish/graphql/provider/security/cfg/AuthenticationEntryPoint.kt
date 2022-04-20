package org.babyfish.graphql.provider.security.cfg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
internal class AuthenticationEntryPoint : ServerAuthenticationEntryPoint {

    private val mapper = jacksonObjectMapper()

    override fun commence(
        exchange: ServerWebExchange,
        ex: AuthenticationException
    ): Mono<Void> {
        if (logger.isErrorEnabled) {
            logger.error("Authenticate failed: ${ex.message}", ex)
        }
        val error = ex.toGraphQLError()
        val buf = DefaultDataBufferFactory().wrap(mapper.writeValueAsBytes(error))
        return exchange.response.writeWith(Mono.just(buf))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationEntryPoint::class.java)
    }
}