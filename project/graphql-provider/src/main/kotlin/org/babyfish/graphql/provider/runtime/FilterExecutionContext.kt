package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.babyfish.graphql.provider.meta.SecurityPredicate
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Filterable
import org.springframework.security.core.Authentication
import java.lang.UnsupportedOperationException

class FilterExecutionContext(
    val prop: GraphQLProp,
    val env: DataFetchingEnvironment,
    val argumentsConverter: ArgumentsConverter,
    val filterable: Filterable<out Entity<*>, *>
) {
    var securityPredicate: SecurityPredicate? = null
}

internal fun filterExecutionContext(): FilterExecutionContext =
    filterContextLocal.get()
        ?: throw UnsupportedOperationException(
            "These functions cannot be called by app directly: " +
                "EntityMapper.Runtime.filterConnection, " +
                "EntityMapper.Runtime.filterList"
        )

internal fun withFilterExecutionContext(
    ctx: FilterExecutionContext,
    block: () -> Unit
) {
    filterContextLocal.set(ctx)
    try {
        block()
    } finally {
        filterContextLocal.remove()
    }
}

private val filterContextLocal =
    ThreadLocal<FilterExecutionContext>()