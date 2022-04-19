package org.babyfish.graphql.provider.security.jwt.cfg

import org.babyfish.graphql.provider.security.AsyncUserDetailsPasswordService
import org.babyfish.graphql.provider.security.AsyncUserDetailsService
import org.babyfish.graphql.provider.security.cfg.AsyncUserDetailsPasswordServiceWrapper
import org.babyfish.graphql.provider.security.cfg.AsyncUserDetailsServiceWrapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
open class ToolConfiguration {

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder::class)
    open fun passwordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    @ConditionalOnMissingBean(ReactiveUserDetailsService::class)
    open fun userDetailsService(
        asyncUserDetailsService: AsyncUserDetailsService<*>?
    ): ReactiveUserDetailsService =
        AsyncUserDetailsServiceWrapper(asyncUserDetailsService)

    @Bean
    @ConditionalOnMissingBean(ReactiveUserDetailsPasswordService::class)
    open fun userDetailsPasswordService(
        asyncUserDetailsPasswordService: AsyncUserDetailsPasswordService<*>?
    ): ReactiveUserDetailsPasswordService =
        AsyncUserDetailsPasswordServiceWrapper(asyncUserDetailsPasswordService)
}