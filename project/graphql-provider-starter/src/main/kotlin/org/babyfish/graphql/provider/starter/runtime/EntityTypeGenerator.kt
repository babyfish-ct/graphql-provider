package org.babyfish.graphql.provider.starter.runtime

import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.starter.meta.impl.ResolvingPhase
import org.babyfish.kimmer.meta.ImmutableType

internal class EntityTypeGenerator(
    private val entityMappers: List<EntityMapper<*>>
) {
    private val entityTypeMap = mutableMapOf<ImmutableType, EntityTypeImpl>()

    val entityTypes: Map<ImmutableType, EntityType>
        get() = entityTypeMap

    fun generate() {
        for (entityMapper in entityMappers) {
            (entityMapper as EntityMapper<Immutable>).apply {
                val entityType = get(entityMapper.immutableType)
                entityType.isMapped = true
                EntityTypeDSL<Immutable>(entityType).config()
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
