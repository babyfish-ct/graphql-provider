package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.security.SecurityDSL
import org.babyfish.graphql.provider.runtime.FilterExecutionContext
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Filterable

@GraphQLProviderDSL
class FilterDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    private val filterExecutionContext: FilterExecutionContext
) {

    fun security(block: SecurityDSL.() -> Unit) {
        val dsl = SecurityDSL()
        dsl.block()
        filterExecutionContext.securityPredicate = dsl.predicate()
    }

    @Suppress("UNCHECKED_CAST")
    fun db(block: Filterable<E, ID>.() -> Unit) {
        (filterExecutionContext.query as Filterable<E, ID>).block()
    }
}