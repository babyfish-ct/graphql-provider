package org.babyfish.graphql.provider.starter.dgs

import org.babyfish.graphql.provider.security.cfg.AuthenticatedPatternSupplier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
internal open class DgsAuthenticatedPatternSupplier(
    @Value("\${dgs.graphql.path:/graphql}") private val graphqlPath: String
) : AuthenticatedPatternSupplier {

    override fun patterns(): Array<String> =
        arrayOf(graphqlPath)
}