package org.babyfish.graphql.provider.security.cfg

import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component

@Component
open class SecurityChecker(
    private val roleHierarchy: RoleHierarchy?
) {
    open fun check(ctx: SecurityContext?, vararg predicates: SecurityPredicate?) {
        val authorities = ctx
            ?.authentication
            ?.takeIf { it.isAuthenticated && it.principal !== null }
            ?.authorities
            ?.let {
                roleHierarchy?.getReachableGrantedAuthorities(it) ?: it
            }
            ?.map { it.authority }
        for (predicate in predicates) {
            if (predicate !== null && !predicate.check(authorities)) {
                throw AccessDeniedException("Access Denied")
            }
        }
    }
}