package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.QueryType
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.meta.impl.QueryPropImpl
import org.babyfish.graphql.provider.meta.impl.QueryTypeImpl
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.EntityMappingBuilder
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

internal class GraphQLTypeGenerator(
    private val queries: List<Query>,
    private val mappers: List<EntityMapper<out Entity<*>, *>>
) {
    private val _queryType = QueryTypeImpl()

    private var _modelTypeMap: Map<KClass<out Entity<*>>, ModelType>? = null

    val queryType: QueryType
        get() = _queryType

    val modelTypeMap: Map<KClass<out Entity<*>>, ModelType>
        get() = _modelTypeMap ?: error("Internal bug, access modelTypeType too early")

    val dynamicConfigurationRegistry = DynamicConfigurationRegistry()

    @Suppress("UNCHECKED_CAST")
    fun generate(builder: EntityMappingBuilder) {
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
        for (mapper in mappers) {
            (mapper as EntityMapper<Entity<*>, *>).apply {
                val kotlinType = mapper.immutableType.kotlinType as KClass<out Entity<*>>
                val modelType = builder.entity(kotlinType) as ModelTypeImpl
                modelType.isMapped = true
                (mapper as EntityMapper<Entity<String>, String>).apply {
                    EntityTypeDSL<Entity<String>, String>(modelType, builder).config()
                }
            }
        }
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

