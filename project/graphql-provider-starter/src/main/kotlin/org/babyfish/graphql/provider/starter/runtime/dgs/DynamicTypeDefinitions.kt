package org.babyfish.graphql.provider.starter.runtime.dgs

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.meta.MetaProvider
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import org.babyfish.graphql.provider.starter.runtime.TypeDefinitionRegistryGenerator

@DgsComponent
open class DynamicTypeDefinitions(
    private val metaProvider: MetaProvider
) {

    @DgsTypeDefinitionRegistry
    open fun registry(): TypeDefinitionRegistry {
        return TypeDefinitionRegistryGenerator(metaProvider.queryType, metaProvider.entityTypes.values).generate()
    }
}