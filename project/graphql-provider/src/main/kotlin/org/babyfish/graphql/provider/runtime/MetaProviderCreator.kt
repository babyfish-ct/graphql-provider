package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.*
import org.babyfish.graphql.provider.meta.impl.MutationPropImpl
import org.babyfish.graphql.provider.meta.impl.MutationTypeImpl
import org.babyfish.graphql.provider.meta.impl.QueryPropImpl
import org.babyfish.graphql.provider.meta.impl.QueryTypeImpl
import org.babyfish.graphql.provider.meta.impl.invokeByRegistryMode
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

fun createMetaProvider(
    queries: List<Query>,
    mutations: List<Mutation>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>,
    rootImplicitInputTypeMap: Map<KClass<out InputMapper<*, *>>, ImplicitInputType>,
    allImplicitInputTypes: List<ImplicitInputType>
): MetaProvider {
    val queryType = QueryTypeImpl()
    val mutationType = MutationTypeImpl()
    val dynamicConfigurationRegistry = DynamicConfigurationRegistry()
    dynamicConfigurationRegistryScope(dynamicConfigurationRegistry) {
        for (query in queries) {
            for (function in query::class.declaredFunctions) {
                if (function.visibility == KVisibility.PUBLIC) {
                    queryType.props[function.name]?.let { conflict ->
                        throw ModelException("Conflict query functions: '${conflict.function}' and '$function'")
                    }
                    if (function.isSuspend) {
                        throw ModelException("Query function '$function' cannot be suspend")
                    }
                    invokeByRegistryMode(query, function)
                    queryType.props[function.name] = QueryPropImpl(function, modelTypeMap, dynamicConfigurationRegistry)
                }
            }
        }
    }
    for (mutation in mutations) {
        for (function in mutation::class.declaredFunctions) {
            if (function.visibility == KVisibility.PUBLIC) {
                mutationType.props[function.name]?.let { conflict ->
                    throw ModelException("Conflict mutation functions: '${conflict.function}' and '$function'")
                }
                if (!function.isSuspend) {
                    throw ModelException("Mutation function '$function' must be suspend")
                }
                mutationType.props[function.name] = MutationPropImpl(function, modelTypeMap)
            }
        }
    }
    return MetaProvider(queryType, mutationType, modelTypeMap, rootImplicitInputTypeMap, allImplicitInputTypes)
}

