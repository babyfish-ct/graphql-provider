package org.babyfish.graphql.provider.security.jwt.cfg

import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.jwt.*
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationServiceImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ConditionalOnProperty("${GraphQLProviderProperties.Security.Jwt.PROPERTY_PATH}.enabled")
@Import(value = [
    JwtAuthenticationFilter::class,
    JwtTokenConverter::class,
    JwtAuthenticationManager::class,
    JwtTokenService::class,
    JwtAuthenticationServiceImpl::class,
    ToolConfiguration::class,
    RestApiConfiguration::class
])
open class JwtSecurityConfiguration