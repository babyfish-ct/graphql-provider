package org.babyfish.graphql.provider.server.runtime

import org.babyfish.kimmer.Immutable

interface Query<T: Immutable> {

    val table: From<T>

    fun where(predicate: Expression<Boolean>)

    fun orderBy(expression: Expression<*>, descending: Boolean = false)
}