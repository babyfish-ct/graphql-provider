package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.runtime.UserImplementationExecutionContext
import java.util.concurrent.CompletableFuture

interface UserImplementation {
    val arguments: List<Argument>
    fun execute(
        ctx: UserImplementationExecutionContext
    ): CompletableFuture<Any?>
}