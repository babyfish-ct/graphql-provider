package org.babyfish.graphql.provider.meta.impl

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.Filter
import org.babyfish.graphql.provider.runtime.ArgumentsConverter
import org.babyfish.graphql.provider.runtime.FilterExecutionContext
import org.babyfish.graphql.provider.runtime.withFilterExecutionContext
import kotlin.reflect.KFunction

internal class FilterImpl(
    val fnOwner: Any,
    val fn: KFunction<*>,
    override val arguments: List<Argument>
): Filter {

    override fun execute(
        env: DataFetchingEnvironment,
        ctx: FilterExecutionContext,
        argumentsConverter: ArgumentsConverter
    ) {
        val args = argumentsConverter.convert(
            arguments,
            fnOwner,
            env
        )
        withFilterExecutionContext(ctx) {
            fn.call(*args)
        }
    }
}