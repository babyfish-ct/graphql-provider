package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.*

internal class QueryPropImpl internal constructor(
    function: KFunction<*>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>
): RootPropImpl(function, modelTypeMap), QueryProp {

    override val arguments: Arguments
        get() = Arguments.of(function)

    override fun toString(): String =
        function.toString()
}