package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.runtime.FilterExecutionContext

interface Filter {

    val raw: FilterDSL<*, *>.() -> Unit

    val arguments: Arguments

    fun apply(ctx: FilterExecutionContext)
}