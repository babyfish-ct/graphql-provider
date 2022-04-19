package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.meta.Arguments
import org.babyfish.graphql.provider.meta.Filter
import org.babyfish.graphql.provider.runtime.FilterExecutionContext
import org.babyfish.graphql.provider.runtime.withFilterExecutionContext
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction

internal class FilterImpl(
    val entityMapper: EntityMapper<*, *>,
    val fn: KFunction<*>,
    override val raw: FilterDSL<*, *>.() -> Unit
): Filter {

    override val arguments = Arguments.of(fn)

    @Suppress("UNCHECKED_CAST")
    override fun apply(ctx: FilterExecutionContext) {
        val args = ctx.argumentsConverter.convert(
            arguments,
            entityMapper,
            ctx.env
        )
        withFilterExecutionContext(ctx) {
            try {
                fn.callBy(args)
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }
        }
    }
}
