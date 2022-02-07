package org.babyfish.graphql.provider.starter.runtime.dgs

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsTypeDefinitionRegistry
import graphql.schema.idl.TypeDefinitionRegistry
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.QueryService
import org.babyfish.graphql.provider.starter.runtime.EntityTypeGenerator
import org.babyfish.graphql.provider.starter.runtime.TypeDefinitionRegistryGenerator

@DgsComponent
open class DynamicTypeDefinitions(
    private val queryServices: List<QueryService>,
    private val entityMappers: List<EntityMapper<*>>
) {

    @DgsTypeDefinitionRegistry
    open fun registry(): TypeDefinitionRegistry {
        val entityTypes = EntityTypeGenerator(entityMappers).run {
            generate()
            entityTypes
        }
        return TypeDefinitionRegistryGenerator(queryServices, entityTypes.values).generate()
    }
}