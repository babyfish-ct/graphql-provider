package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.runtime.DynamicConfigurationRegistry
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.*

internal class QueryPropImpl internal constructor(
    function: KFunction<*>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>,
    dynamicConfigurationRegistry: DynamicConfigurationRegistry
): RootPropImpl(function, modelTypeMap), QueryProp {

    override val userImplementation: UserImplementation? =
        dynamicConfigurationRegistry.userImplementation(function)

    override val filter: Filter? =
        dynamicConfigurationRegistry.filter(function)

    override val cache: Cache =
        dynamicConfigurationRegistry.cache(function)

    override fun toString(): String =
        function.toString()

    override val securityPredicate: SecurityPredicate?
        get() = TODO("Not yet implemented")
}