package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.graphql.provider.server.runtime.ArgumentExpression
import org.babyfish.graphql.provider.server.runtime.Expression
import org.babyfish.graphql.provider.server.runtime.Query
import org.babyfish.kimmer.Immutable

abstract class ArgumentsDSL<E: Immutable> internal constructor(
    val entityProp: EntityPropImpl
) {
    fun <T: Any> argument(
        name: String,
        type: ArgumentType<T?>,
        block: Query<E>.(T) -> Unit
    ) {
    }

    fun <T> argument(
        name: String,
        type: ArgumentType<T>
    ): Expression<T> =
        ArgumentExpression(name, type)
}