package org.babyfish.graphql.provider.starter.runtime

import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.starter.meta.EntityType
import org.babyfish.graphql.provider.starter.meta.QueryType
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl
import org.babyfish.graphql.provider.starter.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.starter.meta.impl.QueryPropImpl
import org.babyfish.graphql.provider.starter.meta.impl.QueryTypeImpl
import org.babyfish.graphql.provider.starter.meta.impl.ResolvingPhase
import org.babyfish.kimmer.meta.ImmutableType
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

internal class GraphQLTypeGenerator(
    private val queries: List<Query>,
    private val mappers: List<EntityMapper<*>>
) {
    private val _queryType = QueryTypeImpl()

    private val entityTypeMap = mutableMapOf<ImmutableType, EntityTypeImpl>()

    val queryType: QueryType
        get() = _queryType

    val entityTypes: Map<ImmutableType, EntityType>
        get() = entityTypeMap

    val dynamicConfigurationRegistry = DynamicConfigurationRegistry()

    @Suppress("UNCHECKED_CAST")
    fun generate() {
        dynamicConfigurationRegistryScope(dynamicConfigurationRegistry) {
            for (query in queries) {
                for (function in query::class.declaredFunctions) {
                    if (function.visibility == KVisibility.PUBLIC && !function.isSuspend) {
                        _queryType.props[function.name]?.let { conflict ->
                            throw ModelException("Conflict query functions: '${conflict.function}' and '$function'")
                        }
                        invokeByRegistryMode(query, function)
                        _queryType.props[function.name] = QueryPropImpl(this, function)
                    }
                }
            }
            for (mapper in mappers) {
                for (function in mapper::class.declaredFunctions) {
                    if (function.name != "config") {
                        if (function.visibility == KVisibility.PUBLIC && !function.isSuspend) {
                            invokeByRegistryMode(mapper, function)
                        }
                    }
                }
            }
        }
        for (entityMapper in mappers) {
            (entityMapper as EntityMapper<Immutable>).apply {
                val entityType = get(entityMapper.immutableType)
                entityType.isMapped = true
                EntityTypeDSL<Immutable>(entityType).config()
            }
        }
        for (phase in ResolvingPhase.values()) {
            resolve(phase)
        }
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
    dynamicConfigurationRegistryFunScope(function) {
        val args = Array<Any?>(function.parameters.size) { null }
        args[0] = owner
        for (index in it.indices) {
            args[index + 1] = it[index].defaultValue()
        }
        try {
            function.call(*args)
        } catch (ex: InvocationTargetException) {
            if (ex.targetException !is NoReturnValue) {
                throw ex
            }
        }
    }
}

