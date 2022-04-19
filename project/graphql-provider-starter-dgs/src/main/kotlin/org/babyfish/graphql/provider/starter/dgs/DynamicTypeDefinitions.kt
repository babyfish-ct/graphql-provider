package org.babyfish.graphql.provider.starter.dgs

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.runtime.createTypeDefinitionRegistry

@DgsComponent
internal open class DynamicTypeDefinitions(
    private val properties: GraphQLProviderProperties,
    private val metaProvider: MetaProvider
) {

    @DgsTypeDefinitionRegistry
    open fun registry(): TypeDefinitionRegistry =
        metaProvider.createTypeDefinitionRegistry(properties)
}