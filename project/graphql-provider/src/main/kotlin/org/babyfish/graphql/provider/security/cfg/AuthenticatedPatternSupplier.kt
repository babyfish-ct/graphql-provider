package org.babyfish.graphql.provider.security.cfg

interface AuthenticatedPatternSupplier {

    fun patterns(): Array<String>?
}