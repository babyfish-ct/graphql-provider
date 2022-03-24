package org.babyfish.graphql.provider.meta

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.runtime.ArgumentsConverter
import org.babyfish.graphql.provider.runtime.UserImplementationExecutionContext
import java.util.concurrent.CompletableFuture

interface UserImplementation {
    val arguments: List<Argument>
    fun execute(
        env: DataFetchingEnvironment,
        ctx: UserImplementationExecutionContext,
        argumentsConverter: ArgumentsConverter
    ): CompletableFuture<Any?>
}