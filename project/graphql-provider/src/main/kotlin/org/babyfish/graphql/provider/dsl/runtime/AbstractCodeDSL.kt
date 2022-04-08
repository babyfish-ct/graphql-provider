package org.babyfish.graphql.provider.dsl.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.dsl.security.SecurityDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate

@GraphQLProviderDSL
abstract class AbstractCodeDSL internal constructor(): TransactionSupporterDSL() {

    private var securityPredicate: SecurityPredicate? = null

    private var securityConfigured = false

    fun security(block: SecurityDSL.() -> Unit) {
        if (securityConfigured) {
            throw ModelException("'${this::class.simpleName}.security { ... }' can only be specified once")
        }
        val dsl = SecurityDSL()
        dsl.block()
        securityPredicate = dsl.predicate()
        securityConfigured = true
    }

    internal fun predicate(): SecurityPredicate? =
        securityPredicate
}