package org.babyfish.graphql.provider.starter.dgs

import com.netflix.graphql.dgs.DgsCodeRegistry
import com.netflix.graphql.dgs.DgsComponent
import graphql.schema.GraphQLCodeRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.DataFetchers
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.runtime.registryDynamicCodeRegistry
import org.babyfish.graphql.provider.security.jwt.JwtAuthenticationService

@DgsComponent
internal open class DynamicCodeRegistry(
    private val properties: GraphQLProviderProperties,
    private val dataFetchers: DataFetchers,
    private val metaProvider: MetaProvider,
    private val jwtAuthenticationService: JwtAuthenticationService?
) {
    @DgsCodeRegistry
    open fun registry(
        builder: GraphQLCodeRegistry.Builder,
        @Suppress("UNUSED") typeDefinitionRegistry: TypeDefinitionRegistry
    ): GraphQLCodeRegistry.Builder =
        builder.apply {
            registryDynamicCodeRegistry(
                properties,
                dataFetchers,
                metaProvider,
                jwtAuthenticationService
            )
        }
}