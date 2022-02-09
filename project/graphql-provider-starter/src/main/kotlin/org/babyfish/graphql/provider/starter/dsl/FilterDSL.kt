package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.starter.runtime.FilterExecutionContext
import org.babyfish.graphql.provider.starter.runtime.query.DatabaseQuery
import org.babyfish.graphql.provider.starter.runtime.query.Filterable
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class FilterDSL<E: Immutable> internal constructor(
    private val filterExecutionContext: FilterExecutionContext
) {
    @Suppress("UNCHECKED_CAST")
    fun db(block: Filterable<E>.() -> Unit) {
        (filterExecutionContext.query as DatabaseQuery<E>).block()
    }

    fun redis(block: FilterRedisDSL.() -> Unit) {
        FilterRedisDSL(filterExecutionContext.dependencies).block()
    }
}