package org.babyfish.graphql.provider.server.runtime

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KProperty1

interface DatabaseQuery<T: Immutable> {

    val table: From<T>

    fun where(vararg predicates: Expression<Boolean>)

    fun orderBy(expression: Expression<*>, descending: Boolean = false)

    fun orderBy(prop: KProperty1<T, *>, descending: Boolean = false) {
        orderBy(table[prop], descending)
    }
}