package org.babyfish.graphql.provider.starter.dgs

import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.reactive.internal.DgsReactiveRequestData
import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.security.SecurityContextExtractor
import org.babyfish.graphql.provider.security.cfg.HttpHeaderSecurityContextFactory
import org.dataloader.BatchLoaderEnvironment
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.attributeOrNull

@Component
class DgsSecurityContextExtractor : SecurityContextExtractor {

    override fun get(env: DataFetchingEnvironment): SecurityContext? =
        (DgsContext.getRequestData(env) as DgsReactiveRequestData)
            .serverRequest
            ?.attributeOrNull(HttpHeaderSecurityContextFactory.SECURITY_CONTEXT_ATTR_KEY)
            as SecurityContext?
}