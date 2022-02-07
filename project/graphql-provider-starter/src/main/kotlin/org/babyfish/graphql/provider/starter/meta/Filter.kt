package org.babyfish.graphql.provider.starter.meta

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.starter.runtime.FilterExecutionContext

interface Filter {

    val arguments: List<Argument>

    fun execute(
        env: DataFetchingEnvironment,
        ctx: FilterExecutionContext
    )
}