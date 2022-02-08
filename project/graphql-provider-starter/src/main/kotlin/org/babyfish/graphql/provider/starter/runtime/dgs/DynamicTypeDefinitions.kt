package org.babyfish.graphql.provider.starter.runtime.dgs

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import org.babyfish.graphql.provider.starter.runtime.TypeDefinitionRegistryGenerator

@DgsComponent
open class DynamicTypeDefinitions(
    private val queries: List<Query>,
    private val entityMappers: List<EntityMapper<*>>
) {

    @DgsTypeDefinitionRegistry
    open fun registry(): TypeDefinitionRegistry {
        val (queryType, entityTypes) = GraphQLTypeGenerator(queries, entityMappers).run {
            generate()
            queryType to entityTypes.values
        }
        return TypeDefinitionRegistryGenerator(queryType, entityTypes).generate()
    }
}