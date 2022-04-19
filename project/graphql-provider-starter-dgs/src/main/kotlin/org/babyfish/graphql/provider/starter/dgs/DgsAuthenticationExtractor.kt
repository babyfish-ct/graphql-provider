package org.babyfish.graphql.provider.starter.dgs

import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.reactive.internal.DgsReactiveRequestData
import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.security.AuthenticationExtractor
import org.babyfish.graphql.provider.security.cfg.SecurityConfiguration
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.attributeOrNull

@Component
internal open class DgsAuthenticationExtractor : AuthenticationExtractor {

    override fun get(env: DataFetchingEnvironment): Authentication? =
        (DgsContext.getRequestData(env) as DgsReactiveRequestData)
            .serverRequest
            ?.attributeOrNull(SecurityConfiguration.GRAPHQL_AUTHENTICATION_KEY)
            as Authentication?
}