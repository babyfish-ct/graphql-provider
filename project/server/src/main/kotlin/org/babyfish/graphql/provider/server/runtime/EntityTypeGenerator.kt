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
        resolve(ResolvingPhase.SUPER_TYPE)
        resolve(ResolvingPhase.DECLARED_PROPS)
        resolve(ResolvingPhase.PROPS)
        resolve(ResolvingPhase.PROP_TARGET)
        resolve(ResolvingPhase.PROP_MAPPED_BY)
        resolve(ResolvingPhase.ID_PROP)
    }

    private fun resolve(phase: ResolvingPhase) {
        for (entityType in entityTypeMap.values) {
            entityType.resolve(this, phase)
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
