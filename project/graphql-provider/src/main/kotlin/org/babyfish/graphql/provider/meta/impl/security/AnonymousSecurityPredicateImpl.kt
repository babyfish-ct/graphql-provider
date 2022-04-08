package org.babyfish.graphql.provider.meta.impl.security

import org.babyfish.graphql.provider.meta.SecurityPredicate

internal object AnonymousSecurityPredicateImpl : SecurityPredicate {

    override fun check(authorities: Collection<String>?): Boolean =
        authorities === null
}