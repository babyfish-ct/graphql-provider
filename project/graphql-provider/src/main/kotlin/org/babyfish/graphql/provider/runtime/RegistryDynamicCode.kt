package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetcher
import graphql.schema.FieldCoordinates
import graphql.schema.GraphQLCodeRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationService

fun GraphQLCodeRegistry.Builder.registryDynamicCodeRegistry(
    properties: GraphQLProviderProperties,
    dataFetchers: DataFetchers,
    metaProvider: MetaProvider,
    jwtAuthenticationService: JwtAuthenticationService?
) {
    registerAuthenticationApi(properties, jwtAuthenticationService)
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
    jwtAuthenticationService: JwtAuthenticationService?
) {
    val api = properties.security.api
    if (api.graphql && jwtAuthenticationService !== null) {
        api.login.trim().takeIf { it.isNotEmpty() }?.let {
            val coordinates = FieldCoordinates.coordinates("Query", it)
            val dataFetcher = DataFetcher {
                mono(Dispatchers.Unconfined) {
                    val username = it.getArgument<String>(api.usernameArgName)
                    val password = it.getArgument<String>("password")
                    jwtAuthenticationService.login(username, password)
                }.toFuture()
            }
            dataFetcher(coordinates, dataFetcher)
        }
        api.updatePassword.trim().takeIf { it.isNotEmpty() }?.let {
            val coordinates = FieldCoordinates.coordinates("Mutation", it)
            val dataFetcher = DataFetcher {
                mono(Dispatchers.Unconfined) {
                    val oldPassword = it.getArgument<String>("oldPassword")
                    val newPassword = it.getArgument<String>("newPassword")
                    jwtAuthenticationService.updatePassword(oldPassword, newPassword)
                }.toFuture()
            }
            dataFetcher(coordinates, dataFetcher)
        }
        api.refreshAccessToken.trim().takeIf { it.isNotEmpty() }?.let {
            val coordinates = FieldCoordinates.coordinates("Mutation", it)
            val dataFetcher = DataFetcher {
                mono(Dispatchers.Unconfined) {
                    val refreshToken = it.getArgument<String>("refreshToken")
                    jwtAuthenticationService.refreshAccessToken(refreshToken)
                }.toFuture()
            }
            dataFetcher(coordinates, dataFetcher)
        }
    }
}