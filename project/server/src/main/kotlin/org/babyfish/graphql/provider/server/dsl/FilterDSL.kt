package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.server.runtime.DatabaseQuery
import org.babyfish.kimmer.Immutable

class FilterDSL<E: Immutable> internal constructor(
    private val databaseQuery: DatabaseQuery<E>
): DatabaseQuery<E> by databaseQuery {

    fun redis(block: FilterRedisDSL<E>.() -> Unit) {

    }
}