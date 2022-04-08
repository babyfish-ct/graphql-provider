package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.UserImplementation
import org.babyfish.graphql.provider.runtime.UserImplementationExecutionContext
import org.babyfish.graphql.provider.runtime.withUserImplementationExecutionContext
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KFunction

internal class UserImplementationImpl(
    val fnOwner: Any,
    val fn: KFunction<*>,
    override val arguments: List<Argument>
) : UserImplementation {

    override fun execute(
        ctx: UserImplementationExecutionContext
    ): CompletableFuture<Any?> {
        val args = ctx.argumentsConverter.convert(
            arguments,
            fnOwner,
            ctx.env
        )
        try {
            withUserImplementationExecutionContext(ctx) {
                fn.call(*args)
            }
        } catch (ex: InvocationTargetException) {
            if (ex.targetException !is NoReturnValue) {
                throw ex.targetException
            }
        }
        return ctx.result ?: error("Internal bug: '$fn' did not set result")
    }
}