package org.babyfish.graphql.provider.security

import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.core.Authentication

open class SecurityChecker(
    private val properties: GraphQLProviderProperties,
    private val roleHierarchy: RoleHierarchy?
) {
    open fun check(authentication: Authentication?, vararg predicates: SecurityPredicate?) {
        val authorities = authentication
            ?.takeIf { auth ->
                auth.isAuthenticated && !properties.security.anonymous.let {
                    it.enabled && it.principal == auth.principal
                }
            }
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