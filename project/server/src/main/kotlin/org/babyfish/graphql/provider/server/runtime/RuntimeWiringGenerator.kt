package org.babyfish.graphql.provider.server.runtime

import graphql.schema.idl.RuntimeWiring
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.kimmer.Immutable

class RuntimeWiringGenerator(
    private val queryServices: Collection<QueryService>,
    private val entityTypes: List<EntityType>
) {

    fun generate(): RuntimeWiring {
        return RuntimeWiring.newRuntimeWiring().apply {
            for (entityType in entityTypes) {
                if (entityType.derivedTypes.isNotEmpty()) {
                    type(entityType.name) { builder ->
                        builder.typeResolver { env ->
                            val obj = env.getObject<Immutable>()
                            val type = entityType.derivedTypes.first { it.kotlinType.java.isInstance(obj) }
                            env.schema.getObjectType(type.name)
                        }
                    }
                }
            }
        }.build()
    }
}