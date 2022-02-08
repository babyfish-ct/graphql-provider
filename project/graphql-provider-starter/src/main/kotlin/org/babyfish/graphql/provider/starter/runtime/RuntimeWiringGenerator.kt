package org.babyfish.graphql.provider.starter.runtime

import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.kimmer.Immutable

internal class RuntimeWiringGenerator(
    private val entityTypes: List<EntityType>
) {

    fun generate(): RuntimeWiring {
        return RuntimeWiring.newRuntimeWiring().apply {
            for (entityType in entityTypes) {
                type(entityType.name) { builder ->
                    builder.apply {
                        generateTypeResolver(entityType)
                    }
                }
            }
        }.build()
    }

    private fun TypeRuntimeWiring.Builder.generateTypeResolver(
        entityType: EntityType
    ) {
        if (entityType.derivedTypes.isNotEmpty()) {
            typeResolver { env ->
                val obj = env.getObject<Immutable>()
                val type = entityType.derivedTypes.first { it.kotlinType.java.isInstance(obj) }
                env.schema.getObjectType(type.name)
            }
        }
    }
}