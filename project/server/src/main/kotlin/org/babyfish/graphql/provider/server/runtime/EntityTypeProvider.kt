package org.babyfish.graphql.provider.server.runtime

import graphql.schema.GraphQLSchema
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.graphql.provider.server.meta.impl.ResolvingPhase
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass

class EntityTypeProvider(
    queryServices: List<QueryService>,
    assemblers: List<EntityAssembler<*>>
) {
    private val entityTypeMap: Map<Class<*>, EntityType>

    val schema: GraphQLSchema

    init {
        val map = mutableMapOf<Class<*>, EntityTypeImpl>()
        val assemblerGroups = assemblers.groupBy {
            GenericTypeResolver.resolveTypeArgument(
                it::class.java,
                EntityAssembler::class.java
            ) as Class<out Immutable>
        }
        for (assemblerGroup in assemblerGroups) {
            val type = assemblerGroup.key
            val entityImpl = EntityTypeImpl(type.kotlin)
            for (assembler in assemblerGroup.value) {
                (assembler as EntityAssembler<Immutable>).apply {
                    EntityTypeDSL<Immutable>(entityImpl).assemble()
                }
            }
            map[type] = entityImpl
        }
        entityTypeMap = map
        for (entityType in map.values) {
            entityType.resolve(this, ResolvingPhase.SUPER_TYPE)
        }
        for (entityType in map.values) {
            entityType.resolve(this, ResolvingPhase.DECLARED_PROPS)
        }
        for (entityType in map.values) {
            entityType.resolve(this, ResolvingPhase.PROPS)
        }
        for (entityType in map.values) {
            entityType.resolve(this, ResolvingPhase.PROP_DETAIL)
        }
        for (entityType in map.values) {
            entityType.resolve(this, ResolvingPhase.ID_PROP)
        }
        schema = SchemaGenerator(queryServices, entityTypeMap.values).generate()
    }

    operator fun get(type: KClass<out Immutable>): EntityType =
        entityTypeMap[type.java] ?: error("No entity assembler for entity type '${type}'")

    fun tryGet(type: KClass<out Immutable>): EntityType? =
        entityTypeMap[type.java]
}
