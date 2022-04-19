package org.babyfish.graphql.provider.runtime.loader

import org.babyfish.graphql.provider.runtime.graphqlMono
import org.dataloader.MappedBatchLoader
import org.springframework.security.core.Authentication
import java.util.concurrent.CompletionStage

internal class UserImplementationLoader(
    private val authentication: Authentication?,
    private val block: suspend (Set<Any>) -> Map<Any, Any>
) : MappedBatchLoader<Any, Any> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, Any>> =
        graphqlMono(null, null, authentication) {
            block(keys)
        }.toFuture()
}