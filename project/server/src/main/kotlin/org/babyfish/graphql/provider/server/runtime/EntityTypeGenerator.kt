package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.EntityMapper
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.server.meta.impl.ResolvingPhase
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.KClass

internal class EntityTypeGenerator(
    private val queryServices: List<QueryService>,
    private val assemblers: List<EntityMapper<*>>
) {
    private val entityTypeMap = mutableMapOf<ImmutableType, EntityTypeImpl>()

    val entityTypes: Map<ImmutableType, EntityType>
        get() = entityTypeMap

    fun generate() {
        for (assembler in assemblers) {
            (assembler as EntityMapper<Immutable>).apply {
                val entityType = get(assembler.immutableType)
                entityType.isAssembled = true
                EntityTypeDSL<Immutable>(entityType).map()
            }
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.SUPER_TYPE)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.DECLARED_PROPS)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.PROPS)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.PROP_DETAIL)
        }
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, ResolvingPhase.ID_PROP)
        }
    }

    operator fun get(immutableType: ImmutableType): EntityTypeImpl =
        entityTypeMap[immutableType] ?: create(immutableType)

    private fun create(immutableType: ImmutableType): EntityTypeImpl {
        val entityType = EntityTypeImpl(immutableType)
        entityTypeMap[immutableType] = entityType
        for (superType in immutableType.superTypes) {
            this[superType]
        }
        for (prop in immutableType.declaredProps.values) {
            prop.targetType?.let {
                this[it]
            }
        }
        return entityType
    }
}
