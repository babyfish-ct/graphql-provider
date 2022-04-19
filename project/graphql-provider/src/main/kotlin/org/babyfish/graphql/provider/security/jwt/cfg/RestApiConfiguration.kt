package org.babyfish.graphql.provider.security.jwt.cfg

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import kotlinx.coroutines.reactor.awaitSingle
import org.babyfish.graphql.provider.runtime.ExecutorContext
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.runtime.withExecutorContext
import org.babyfish.graphql.provider.security.cfg.toGraphQLError
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationManager
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationService
import org.babyfish.graphql.provider.security.jwt.JwtTokenConverter
import org.springdoc.core.GroupedOpenApi
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
@ConditionalOnExpression(RestApiConfiguration.SPEL)
internal open class RestApiConfiguration(
    private val properties: GraphQLProviderProperties,
    private val jwtAuthenticationService: JwtAuthenticationService,
    private val jwtTokenConverter: JwtTokenConverter,
    private val jwtAuthenticationManager: JwtAuthenticationManager
) {

    @Bean
    @RouterOperations(
        RouterOperation(
            path = "$DOC_BASE/login",
            method = arrayOf(RequestMethod.GET),
            parameterTypes = [String::class, String::class],
            operation = Operation(
                parameters = [
                    Parameter(name = "username", required = true),
                    Parameter(name = "password", required = true)
                ]
            )
        )
    )
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
            api.updatePassword.trim().takeIf { it.isNotEmpty() }?.let { fn->
                PUT("${api.restPath}/$fn") {
                    val jwtToken = jwtTokenConverter.convert(it.exchange()).awaitSingle()
                    val authentication = jwtAuthenticationManager.authenticate(jwtToken).awaitSingle()
                    withExecutorContext(ExecutorContext(null, null, authentication)) {
                        val oldPassword = it.queryParam("oldPassword").get()
                        val newPassword = it.queryParam("newPassword").get()
                        try {
                            ServerResponse.ok()
                                .bodyValueAndAwait(
                                    jwtAuthenticationService.updatePassword(oldPassword, newPassword)
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
            api.refreshAccessToken.trim().takeIf { it.isNotEmpty() }?.let { fn->
                PUT("${api.refreshAccessToken}/$fn") {
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

    @Bean
    open fun storeOpenApi(): GroupedOpenApi? {
        val path = properties.security.api.restPath
        val paths = arrayOf("/${path}/**")
        return GroupedOpenApi.builder().group(path).pathsToMatch(*paths)
            .build()
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
