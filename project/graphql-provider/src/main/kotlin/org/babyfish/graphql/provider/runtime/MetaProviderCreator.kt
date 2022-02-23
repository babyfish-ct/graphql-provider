package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.invokeByRegistryMode
import org.babyfish.graphql.provider.meta.impl.QueryPropImpl
import org.babyfish.graphql.provider.meta.impl.QueryTypeImpl
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

internal fun createMetaProvider(
    queries: List<Query>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>
): MetaProvider {
    val queryType = QueryTypeImpl()
    val dynamicConfigurationRegistry = DynamicConfigurationRegistry()
    dynamicConfigurationRegistryScope(dynamicConfigurationRegistry) {
        for (query in queries) {
            for (function in query::class.declaredFunctions) {
                if (function.visibility == KVisibility.PUBLIC && !function.isSuspend) {
                    queryType.props[function.name]?.let { conflict ->
                        throw ModelException("Conflict query functions: '${conflict.function}' and '$function'")
                    }
                    invokeByRegistryMode(query, function)
                    queryType.props[function.name] = QueryPropImpl(function, modelTypeMap, dynamicConfigurationRegistry)
                }
            }
        }
    }
    return MetaProvider(queryType, modelTypeMap)
}

