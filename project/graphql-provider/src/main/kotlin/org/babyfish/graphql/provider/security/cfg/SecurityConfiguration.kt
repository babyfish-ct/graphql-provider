package org.babyfish.graphql.provider.security.cfg

import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationFilter
import org.babyfish.graphql.provider.security.jwt.cfg.JwtSecurityConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@Import(value = [
    CorsConfigurer::class,
    AuthenticationEntryPoint::class,
    JwtSecurityConfiguration::class
])
open class SecurityConfiguration internal constructor(
    private val authenticatedPatternSupplier: AuthenticatedPatternSupplier?,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter?
) {

    @Bean
    open fun securityWebFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain =
        http
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
                it.authenticationEntryPoint(AuthenticationEntryPoint())
            }
            .apply {
                jwtAuthenticationFilter?.let {
                    addFilterAt(it, SecurityWebFiltersOrder.AUTHENTICATION)
                }
            }
            .authorizeExchange {
                it
                    .apply {
                        if (jwtAuthenticationFilter !== null) {
                            pathMatchers(
                                *(authenticatedPatternSupplier?.patterns() ?: arrayOf("/graphql"))
                            ).authenticated()
                        }
                    }
                    .anyExchange()
                    .permitAll()
            }
            .build()

    @Bean
    @ConditionalOnMissingBean(ReactiveAuthenticationManager::class)
    open fun disabledAuthenticationManager(): ReactiveAuthenticationManager =
        ReactiveAuthenticationManager { Mono.empty() }

    companion object {
        const val GRAPHQL_AUTHENTICATION_KEY = "GRAPHQL_AUTHENTICATION"
    }
}