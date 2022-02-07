package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.starter.runtime.query.DatabaseQuery
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class FilterDSL<E: Immutable> internal constructor(
    private val databaseQuery: DatabaseQuery<E>
) {
    fun db(block: DatabaseQuery<E>.() -> Unit) {

    }

    fun redis(block: FilterRedisDSL.() -> Unit) {

    }
}