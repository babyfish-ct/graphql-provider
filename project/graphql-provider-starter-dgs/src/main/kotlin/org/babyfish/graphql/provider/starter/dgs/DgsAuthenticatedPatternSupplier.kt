package org.babyfish.graphql.provider.starter.dgs

import org.babyfish.graphql.provider.security.cfg.AuthenticatedPatternSupplier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DgsAuthenticatedPatternSupplier(
    @Value("dgs.graphql.path") private val graphqlPath: String?
) : AuthenticatedPatternSupplier {

    override fun patterns(): Array<String>? =
        graphqlPath?.let { arrayOf(it) }
}