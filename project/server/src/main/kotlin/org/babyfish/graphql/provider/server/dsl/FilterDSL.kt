package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.server.runtime.query.DatabaseQuery
import org.babyfish.kimmer.Immutable

class FilterDSL<E: Immutable> internal constructor(
    private val databaseQuery: DatabaseQuery<E>
) {
    fun db(block: DatabaseQuery<E>.() -> Unit) {

    }

    fun redis(block: FilterRedisDSL.() -> Unit) {

    }

    fun <T> String.whenNotBlank(block: (String) -> T): T? =
        if (this != "") {
            block(this)
        } else {
            null
        }
}