package org.babyfish.graphql.provider.meta.impl.security

import org.babyfish.graphql.provider.meta.SecurityPredicate

internal class CompositeSecurityPredicateImpl private constructor(
    private val and: Boolean,
    private val predicates: Collection<SecurityPredicate>
): SecurityPredicate {

    override fun check(authorities: Collection<String>?): Boolean =
        if (and) and(authorities) else or(authorities)

    private fun and(roles: Collection<String>?): Boolean {
        for (predicate in predicates) {
            if (!predicate.check(roles)) {
                return false
            }
        }
        return true
    }

    private fun or(roles: Collection<String>?): Boolean {
        for (predicate in predicates) {
            if (predicate.check(roles)) {
                return true
            }
        }
        return false
    }

    companion object {
        fun of(and: Boolean, predicates: Collection<SecurityPredicate>): SecurityPredicate? =
            when (predicates.size) {
                0 -> null
                1 -> predicates.first()
                else -> CompositeSecurityPredicateImpl(and, predicates)
            }
    }
}