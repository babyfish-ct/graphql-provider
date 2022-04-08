package org.babyfish.graphql.provider.security.cfg

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import reactor.core.publisher.Mono

@Configuration
@Import(value = [
    CorsConfigurer::class,
    HttpHeaderSecurityContextFactory::class,
    NoOpAuthenticationManager::class,
    SecurityChecker::class
])
open class SecurityConfiguration(
    private val authenticationManager: ReactiveAuthenticationManager,
    private val securityContextRepository: ServerSecurityContextRepository,
    private val authenticatedPatternSupplier: AuthenticatedPatternSupplier?
) {
    @Bean
    open fun securityWebFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain =
        http
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .csrf {
                it.disable()
            }
            .formLogin {
                it.disable()
            }
            .httpBasic {
                it.disable()
            }
            .logout {
                it.disable()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { exchange, _ ->
                    Mono.fromRunnable {
                        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    }
                }
                it.accessDeniedHandler { exchange, _ ->
                    Mono.fromRunnable {
                        exchange.response.statusCode = HttpStatus.FORBIDDEN
                    }
                }
            }
            .authorizeExchange {
                it
                    .pathMatchers(
                        *authenticatedPatternSupplier?.patterns() ?: arrayOf("/graphql")
                    )
                    .authenticated()
                    .pathMatchers(HttpMethod.OPTIONS).permitAll()
                    .anyExchange().permitAll()
            }
            .build()
}