package org.babyfish.graphql.provider.runtime

import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.kimmer.Immutable

internal class RuntimeWiringGenerator(
    private val modelTypes: List<ModelType>
) {

    fun generate(): RuntimeWiring {
        return RuntimeWiring.newRuntimeWiring().apply {
            for (entityType in modelTypes) {
                type(entityType.name) { builder ->
                    builder.apply {
                        generateTypeResolver(entityType)
                    }
                }
            }
        }.build()
    }

    private fun TypeRuntimeWiring.Builder.generateTypeResolver(
        modelType: ModelType
    ) {
        if (modelType.derivedTypes.isNotEmpty()) {
            typeResolver { env ->
                val obj = env.getObject<Immutable>()
                val type = modelType.derivedTypes.first { it.kotlinType.java.isInstance(obj) }
                env.schema.getObjectType(type.name)
            }
        }
    }
}