package org.babyfish.graphql.provider.server.runtime

import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

interface From<T: Immutable> {
    operator fun <X> get(prop: KProperty1<T, X?>): Expression<X>
    fun <X: Immutable> reference(prop: KProperty1<T, X?>): From<X>
    fun <X: Immutable> list(prop: KProperty1<T, List<X>?>): From<X>
    fun <X: Immutable> connection(prop: KProperty1<T, Connection<X>?>): From<X>
}