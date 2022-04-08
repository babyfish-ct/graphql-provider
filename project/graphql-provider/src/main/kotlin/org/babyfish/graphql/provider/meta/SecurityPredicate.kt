package org.babyfish.graphql.provider.meta

interface SecurityPredicate {
    fun check(authorities: Collection<String>?): Boolean
}