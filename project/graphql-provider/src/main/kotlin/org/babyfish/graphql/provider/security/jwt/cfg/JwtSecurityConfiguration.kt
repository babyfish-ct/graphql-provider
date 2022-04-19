package org.babyfish.graphql.provider.security.jwt.cfg

import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.AuthenticationBehaviorProvider
import org.babyfish.graphql.provider.security.jwt.*
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationServiceImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.lang.IllegalStateException

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
open class JwtSecurityConfiguration(
    authenticationBehaviorProvider: AuthenticationBehaviorProvider<*>?
) {
    init {
        if (authenticationBehaviorProvider === null) {
            throw IllegalStateException(
                "A bean whose type is '${AuthenticationBehaviorProvider::class.qualifiedName}' " +
                    "must be configured when " +
                    "'${GraphQLProviderProperties.Security.Jwt.PROPERTY_PATH}.enabled' is true"
            )
        }
    }
}