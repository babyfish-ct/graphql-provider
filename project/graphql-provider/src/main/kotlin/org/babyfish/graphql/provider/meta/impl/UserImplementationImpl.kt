package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.Arguments
import org.babyfish.graphql.provider.meta.UserImplementation
import org.babyfish.graphql.provider.runtime.UserImplementationExecutionContext
import org.babyfish.graphql.provider.runtime.withUserImplementationExecutionContext
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KFunction

class UserImplementationImpl(
    val entityMapper: EntityMapper<*, *>,
    val fn: KFunction<*>
): UserImplementation {

    override val arguments = Arguments.of(fn)

    override fun execute(
        ctx: UserImplementationExecutionContext
    ): CompletableFuture<Any?> {
        val args = ctx.argumentsConverter.convert(
            arguments,
            entityMapper,
            ctx.env
        )
        withUserImplementationExecutionContext(ctx) {
            try {
                fn.callBy(args)
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }
        }
        return ctx.result ?: error("Internal bug: '$fn' did not set result")
    }
}