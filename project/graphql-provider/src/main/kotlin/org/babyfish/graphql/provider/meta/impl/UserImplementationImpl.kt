package org.babyfish.graphql.provider.meta.impl

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.UserImplementation
import org.babyfish.graphql.provider.runtime.ArgumentsConverter
import org.babyfish.graphql.provider.runtime.UserImplementationExecutionContext
import org.babyfish.graphql.provider.runtime.withUserImplementationExecutionContext
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KFunction

internal class UserImplementationImpl(
    val fnOwner: Any,
    val fn: KFunction<*>,
    override val arguments: List<Argument>
) : UserImplementation {

    override fun execute(
        env: DataFetchingEnvironment,
        ctx: UserImplementationExecutionContext,
        argumentsConverter: ArgumentsConverter
    ): CompletableFuture<Any?> {
        val args = argumentsConverter.convert(
            arguments,
            fnOwner,
            env
        )
        withUserImplementationExecutionContext(ctx) {
            fn.call(*args)
        }
        return ctx.result ?: error("Internal bug: '$fn' did not set result")
    }
}