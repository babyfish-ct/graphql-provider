package org.babyfish.graphql.provider.runtime.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletionStage

internal class UserImplementationLoader(
    private val block: suspend (Set<Any>) -> Map<Any, Any?>
) : MappedBatchLoader<Any, Any?> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, Any?>> =
        mono(Dispatchers.Unconfined) {
            block(keys)
        }.toFuture()
}