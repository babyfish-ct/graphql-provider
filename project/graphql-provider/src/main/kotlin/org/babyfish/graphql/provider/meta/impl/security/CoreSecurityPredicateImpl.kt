package org.babyfish.graphql.provider.meta.impl.security

import org.babyfish.graphql.provider.meta.SecurityPredicate

internal class CoreSecurityPredicateImpl private constructor(
    private val authorities: Set<String>
): SecurityPredicate {

    override fun check(authorities: Collection<String>?): Boolean {
        val checkedAuthorities = authorities ?: return false
        for (checkedAuthority in checkedAuthorities) {
            if (this.authorities.contains(checkedAuthority)) {
                return true
            }
        }
        return false
    }

    companion object {
        fun of(authorities: Set<String>): SecurityPredicate? =
            authorities
                .takeIf { it.isNotEmpty() }
                ?.let { CoreSecurityPredicateImpl(it) }
    }
}