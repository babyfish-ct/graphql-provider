package org.babyfish.graphql.provider.meta

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.runtime.FilterExecutionContext

interface Filter {

    val arguments: List<Argument>

    fun execute(
        env: DataFetchingEnvironment,
        ctx: FilterExecutionContext
    )
}