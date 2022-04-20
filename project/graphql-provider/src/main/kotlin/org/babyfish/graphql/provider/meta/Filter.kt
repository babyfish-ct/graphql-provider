package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.runtime.FilterExecutionContext
import org.springframework.security.core.Authentication

interface Filter {

    val raw: FilterDSL<*, *>.(Authentication?) -> Unit

    val arguments: Arguments

    fun apply(ctx: FilterExecutionContext)
}