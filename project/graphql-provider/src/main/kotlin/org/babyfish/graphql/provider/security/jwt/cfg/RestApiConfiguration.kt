package org.babyfish.graphql.provider.security.jwt.cfg

import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.cfg.toGraphQLError
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationService
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
@ConditionalOnExpression(RestApiConfiguration.SPEL)
internal open class RestApiConfiguration(
    private val properties: GraphQLProviderProperties,
    private val jwtAuthenticationService: JwtAuthenticationService
) {

    @Bean
    open fun authenticationRouter() =
        coRouter {
            val api = properties.security.api
            api.login.trim().takeIf { it.isNotEmpty() }?.let { fn ->
                GET("${api.restPath}/$fn") {
                    val username = it.queryParam(api.usernameArgName).get()
                    val password = it.queryParam("password").get()
                    try {
                        ServerResponse.ok()
                            .bodyValueAndAwait(
                                jwtAuthenticationService.login(username, password)
                            )
                    } catch (ex: Throwable) {
                        ServerResponse.status(HttpStatus.BAD_GATEWAY)
                            .bodyValueAndAwait(
                                ex.toGraphQLError()
                            )
                    }
                }
            }
            api.refreshAccessToken.trim().takeIf { it.isNotEmpty() }?.let { fn->
                GET("${api.restPath}/$fn") {
                    val refreshToken = it.queryParam("refreshToken").get()
                    try {
                        ServerResponse.ok()
                            .bodyValueAndAwait(
                                jwtAuthenticationService.refreshAccessToken(refreshToken)
                            )
                    } catch (ex: Throwable) {
                        ServerResponse.status(HttpStatus.BAD_GATEWAY)
                            .bodyValueAndAwait(
                                ex.toGraphQLError()
                            )
                    }
                }
            }
        }

    companion object {

        private const val REST_PATH = "restPath"

        private const val REST_FULL_PATH = "${GraphQLProviderProperties.Security.Api.PROPERTY_PATH}.$REST_PATH"

        const val SPEL = "'\${$REST_FULL_PATH}' != ''"

        const val DOC_BASE = "\${$REST_FULL_PATH}"

        init {
            if (GraphQLProviderProperties.Security.Api::restPath.name != REST_PATH) {
                error("Internal bug: restPath is refactored")
            }
        }
    }
}
