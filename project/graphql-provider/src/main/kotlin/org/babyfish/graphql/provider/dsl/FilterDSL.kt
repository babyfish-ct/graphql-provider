package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.security.SecurityDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Filterable

@GraphQLProviderDSL
class FilterDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    private val filterable: Filterable<E, ID>
) {

    private var securityConfigured = false

    private var securityPredicate: SecurityPredicate? = null

    @Suppress("UNCHECKED_CAST")
    fun db(block: Filterable<E, ID>.() -> Unit) {
        filterable.block()
    }

    fun security(block: SecurityDSL.() -> Unit) {
        if (securityConfigured) {
            throw ModelException(
                "'${this::class.simpleName}.security { ... }' can only be specified once"
            )
        }
        val dsl = SecurityDSL()
        dsl.block()
        securityPredicate = dsl.predicate()
        securityConfigured = true
    }

    internal fun predicate(): SecurityPredicate? =
        securityPredicate
}