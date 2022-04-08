package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.runtime.FilterExecutionContext

interface Filter {

    val arguments: List<Argument>

    fun execute(
        ctx: FilterExecutionContext
    )
}