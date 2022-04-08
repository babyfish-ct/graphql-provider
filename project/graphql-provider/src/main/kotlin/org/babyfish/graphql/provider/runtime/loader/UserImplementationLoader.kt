package org.babyfish.graphql.provider.runtime.loader

import org.babyfish.graphql.provider.security.executeWithSecurityContext
import org.dataloader.MappedBatchLoader
import org.springframework.security.core.context.SecurityContext
import java.util.concurrent.CompletionStage

internal class UserImplementationLoader(
    private val securityContext: SecurityContext?,
    private val block: suspend (Set<Any>) -> Map<Any, Any>
) : MappedBatchLoader<Any, Any> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, Any>> =
        executeWithSecurityContext(securityContext) {
            block(keys)
        }.toFuture()
}