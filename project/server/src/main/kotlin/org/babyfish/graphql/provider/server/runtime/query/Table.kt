package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

interface Table<T: Immutable> {

    operator fun <X> get(
        prop: KProperty1<T, X?>
    ): Expression<X>
}