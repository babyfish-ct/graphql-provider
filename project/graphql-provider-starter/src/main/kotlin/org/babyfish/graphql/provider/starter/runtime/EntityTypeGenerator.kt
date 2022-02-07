package org.babyfish.graphql.provider.starter.runtime

import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.starter.meta.impl.ResolvingPhase
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

internal class EntityTypeGenerator(
    private val entityMappers: List<EntityMapper<*>>
) {
    private val entityTypeMap = mutableMapOf<ImmutableType, EntityTypeImpl>()

    val entityTypes: Map<ImmutableType, EntityType>
        get() = entityTypeMap

    fun generate() {
        filterRegistryScope {
            for (entityMapper in entityMappers) {
                for (function in entityMapper::class.declaredFunctions) {
                    if (function.name != "config") {
                        if (function.visibility == KVisibility.PUBLIC) {
                            invokeByRegistryMode(entityMapper, function)
                        }
                    }
                }
            }
        }
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

private fun invokeByRegistryMode(owner: Any, function: KFunction<*>) {
    filterRegistryFunScope(function) {
        val args = Array<Any?>(function.parameters.size) { null }
        args[0] = owner
        for (index in it.indices) {
            args[index + 1] = it[index].defaultValue()
        }
        function.call(*args)
    }
}

