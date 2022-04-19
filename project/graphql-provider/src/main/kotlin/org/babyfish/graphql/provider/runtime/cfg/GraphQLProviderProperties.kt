package org.babyfish.graphql.provider.runtime.cfg

import org.babyfish.graphql.provider.meta.ModelProp
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.http.HttpHeaders
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(GraphQLProviderProperties.PROPERTY_PATH)
data class GraphQLProviderProperties(
    val defaultBatchSize: Int = 128,
    val defaultCollectionBatchSize: Int = 16,
    val security: Security = Security()
) {
    companion object {
        const val PROPERTY_PATH = "graphql.provider"
    }

    fun batchSize(prop: ModelProp): Int =
        prop.batchSize
            ?: if (prop.isConnection || prop.isList) {
                prop.declaringType.graphql.defaultCollectionBatchSize
                    ?: defaultCollectionBatchSize
            } else {
                prop.declaringType.graphql.defaultBatchSize
                    ?: defaultBatchSize
            }

    @ConfigurationProperties(Security.PROPERTY_PATH)
    @ConstructorBinding
    data class Security(
        val anonymous: Anonymous = Anonymous(),
        val jwt: Jwt = Jwt(),
        val api: Api = Api()
    ) {
        companion object {
            const val PROPERTY_PATH = "${GraphQLProviderProperties.PROPERTY_PATH}.security"
        }

        init {
            if (!anonymous.enabled && api.graphql) {
                throw IllegalArgumentException(
                    "${Api.PROPERTY_PATH}.${Api::graphql.name} cannot be true(or default) " +
                        "when ${Anonymous.PROPERTY_PATH}.${Anonymous::enabled} is false"
                )
            }
        }

        @ConfigurationProperties(Anonymous.PROPERTY_PATH)
        @ConstructorBinding
        data class Anonymous(
            val enabled: Boolean = true,
            val principal: String = "<<anonymous>>",
            val roles: List<String> = listOf("ROLE_ANONYMOUS")
        ) {
            companion object {
                const val PROPERTY_PATH = "${Security.PROPERTY_PATH}.anonymous"
            }

            init {
                if (principal == "") {
                    throw IllegalArgumentException("'$PROPERTY_PATH' cannot be empty")
                }
            }
        }

        @ConfigurationProperties(Jwt.PROPERTY_PATH)
        @ConstructorBinding
        data class Jwt(
            val enabled: Boolean = false,
            val header: String = HttpHeaders.AUTHORIZATION,
            val secret: String = "",
            val thinAuthentication: Boolean = false,
            val accessTimeout: Duration = Duration.ofMinutes(5),
            val refreshTimeout: Duration = Duration.ofMinutes(30)
        ) {
            companion object {
                const val PROPERTY_PATH = "${Security.PROPERTY_PATH}.jwt"
            }

            init {
                if (header == "") {
                    throw IllegalArgumentException(
                        "'${PROPERTY_PATH}.${Jwt::header.name}' cannot be empty"
                    )
                }
                if (accessTimeout >= refreshTimeout) {
                    throw IllegalArgumentException(
                        "'${PROPERTY_PATH}.${Jwt::accessTimeout.name}' must be less than " +
                            "'${PROPERTY_PATH}.${Jwt::refreshTimeout.name}'"
                    )
                }
            }
        }

        @ConfigurationProperties(Api.PROPERTY_PATH)
        @ConstructorBinding
        data class Api(
            val graphql: Boolean = true,
            val restPath: String = "/authentication",
            val login: String = "login",
            val usernameArgName: String = "username",
            val updatePassword: String = "updatePassword",
            val refreshAccessToken: String = "refreshAccessToken"
        ) {

            init {
                if (restPath == "/") {
                    throw IllegalArgumentException(
                        "'${PROPERTY_PATH}.${Api::restPath.name}' cannot be '/'"
                    )
                }
                if (restPath != "") {
                    if (!restPath.startsWith("/")) {
                        throw IllegalArgumentException(
                            "'${PROPERTY_PATH}.${Api::restPath.name}' must start with '/' when it's not empty"
                        )
                    }
                    if (restPath.endsWith("/")) {
                        throw IllegalArgumentException(
                            "'${PROPERTY_PATH}.${Api::restPath.name}' cannot end with '/'"
                        )
                    }
                }
                if (usernameArgName.isEmpty()) {
                    throw IllegalArgumentException(
                        "'${PROPERTY_PATH}.${Api::usernameArgName.name}' cannot be empty"
                    )
                }
            }
            companion object {
                const val PROPERTY_PATH = "${Security.PROPERTY_PATH}.api"
            }
        }
    }
}