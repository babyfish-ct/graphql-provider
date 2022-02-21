package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.runtime.FilterExecutionContext
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Filterable

@GraphQLProviderDSL
class FilterDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    private val filterExecutionContext: FilterExecutionContext
) {
    @Suppress("UNCHECKED_CAST")
    fun db(block: Filterable<E, ID>.() -> Unit) {
        (filterExecutionContext.query as Filterable<E, ID>).block()
    }

    fun redis(block: FilterRedisDSL.() -> Unit) {
        FilterRedisDSL(filterExecutionContext.dependencies).block()
    }
}