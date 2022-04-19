package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.runtime.UserImplementationExecutionContext
import java.util.concurrent.CompletableFuture

interface UserImplementation {

    val arguments: Arguments

    fun execute(
        ctx: UserImplementationExecutionContext
    ): CompletableFuture<Any?>
}