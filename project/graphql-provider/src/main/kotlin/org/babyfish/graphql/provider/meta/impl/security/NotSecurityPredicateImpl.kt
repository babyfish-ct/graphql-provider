package org.babyfish.graphql.provider.meta.impl.security

import org.babyfish.graphql.provider.meta.SecurityPredicate

internal class NotSecurityPredicateImpl(
    private val predicate: SecurityPredicate
): SecurityPredicate {

    override fun check(authorities: Collection<String>?): Boolean =
        !predicate.check(authorities)
}