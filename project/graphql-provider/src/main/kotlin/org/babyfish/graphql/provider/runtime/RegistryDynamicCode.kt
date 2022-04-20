package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.AuthenticationExtractor
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationService

fun GraphQLCodeRegistry.Builder.registryDynamicCodeRegistry(
    properties: GraphQLProviderProperties,
    dataFetchers: DataFetchers,
    metaProvider: MetaProvider,
    jwtAuthenticationService: JwtAuthenticationService?,
    authenticationExtractor: AuthenticationExtractor?
) {
    registerAuthenticationApi(
        properties,
        jwtAuthenticationService,
        authenticationExtractor
    )
    for (prop in metaProvider.queryType.props.values) {
        val coordinates = FieldCoordinates.coordinates("Query", prop.name)
        val dataFetcher = DataFetcher {
            dataFetchers.fetch(prop, it)
        }
        dataFetcher(coordinates, dataFetcher)
    }
    for (prop in metaProvider.mutationType.props.values) {
        val coordinates = FieldCoordinates.coordinates("Mutation", prop.name)
        val dataFetcher = DataFetcher {
            dataFetchers.fetch(prop, it)
        }
        dataFetcher(coordinates, dataFetcher)
    }
    for (modelType in metaProvider.modelTypes.values) {
        for (prop in modelType.declaredProps.values) {
            if (prop.userImplementation !== null ||
                prop.isAssociation ||
                prop.securityPredicate !== null ||
                prop.declaringType.securityPredicate !== null
            ) {
                val coordinates = FieldCoordinates.coordinates(modelType.name, prop.name)
                val dataFetcher = DataFetcher {
                    dataFetchers.fetch(prop, it)
                }
                dataFetcher(coordinates, dataFetcher)
            }
        }
    }
}

private fun GraphQLCodeRegistry.Builder.registerAuthenticationApi(
    properties: GraphQLProviderProperties,
    jwtAuthenticationService: JwtAuthenticationService?,
    authenticationExtractor: AuthenticationExtractor?
) {
    val api = properties.security.api
    if (api.graphql && jwtAuthenticationService !== null) {
        api.login.trim().takeIf { it.isNotEmpty() }?.let { fn ->
            val coordinates = FieldCoordinates.coordinates("Query", fn)
            val dataFetcher = DataFetcher { it
                graphqlMono(ExecutorContext(null, it, authenticationExtractor?.get(it))) {
                    val username = it.getArgument<String>(api.usernameArgName)
                    val password = it.getArgument<String>("password")
                    jwtAuthenticationService.login(username, password)
                }.toFuture()
            }
            dataFetcher(coordinates, dataFetcher)
        }
        api.refreshAccessToken.trim().takeIf { it.isNotEmpty() }?.let { fn ->
            val coordinates = FieldCoordinates.coordinates("Query", fn)
            val dataFetcher = DataFetcher {
                graphqlMono(ExecutorContext(null, it, authenticationExtractor?.get(it))) {
                    val refreshToken = it.getArgument<String>("refreshToken")
                    jwtAuthenticationService.refreshAccessToken(refreshToken)
                }.toFuture()
            }
            dataFetcher(coordinates, dataFetcher)
        }
        api.updatePassword.trim().takeIf { it.isNotEmpty() }?.let { fn ->
            val coordinates = FieldCoordinates.coordinates("Mutation", fn)
            val dataFetcher = DataFetcher {
                graphqlMono(ExecutorContext(null, it, authenticationExtractor?.get(it))) {
                    val oldPassword = it.getArgument<String>("oldPassword")
                    val newPassword = it.getArgument<String>("newPassword")
                    jwtAuthenticationService.updatePassword(oldPassword, newPassword)
                }.toFuture()
            }
            dataFetcher(coordinates, dataFetcher)
        }
    }
}