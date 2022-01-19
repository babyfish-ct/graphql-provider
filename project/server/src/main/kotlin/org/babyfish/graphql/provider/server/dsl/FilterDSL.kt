package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.runtime.ArgumentExpression
import org.babyfish.graphql.provider.server.runtime.Expression
import org.babyfish.graphql.provider.server.runtime.Query
import org.babyfish.kimmer.Immutable

class FilterDSL<E: Immutable> internal constructor() {

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


    fun default(block: Query<E>.() -> Unit) {

    }
}