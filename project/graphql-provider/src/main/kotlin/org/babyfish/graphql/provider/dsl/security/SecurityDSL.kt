package org.babyfish.graphql.provider.dsl.security

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.graphql.provider.meta.impl.security.AnonymousSecurityPredicateImpl
import org.babyfish.graphql.provider.meta.impl.security.CompositeSecurityPredicateImpl
import org.babyfish.graphql.provider.meta.impl.security.NotSecurityPredicateImpl
import org.babyfish.graphql.provider.meta.impl.security.CoreSecurityPredicateImpl

@GraphQLProviderDSL
class SecurityDSL internal constructor(
    private val and: Boolean = false
) {

    private val predicates = mutableListOf<SecurityPredicate>()

    fun anonymous() {
        predicates += AnonymousSecurityPredicateImpl
    }

    fun authorities(vararg authorities: String) {
        CoreSecurityPredicateImpl.of(authorities.toSet())?.let {
            predicates += it
        }
    }

    fun and(block: SecurityDSL.() -> Unit) {
        SecurityDSL(true).run {
            block()
            predicate()
        }?.let {
            predicates += it
        }
    }

    fun or(block: SecurityDSL.() -> Unit) {
        SecurityDSL().run {
            block()
            predicate()
        }?.let {
            predicates += it
        }
    }

    fun not(block: SecurityDSL.() -> Unit) {
        SecurityDSL().run {
            block()
            predicate()
        }?.let {
            predicates += NotSecurityPredicateImpl(it)
        }
    }

    internal fun predicate(): SecurityPredicate? =
        CompositeSecurityPredicateImpl.of(and, predicates)
}