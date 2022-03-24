package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.GraphQLProp
import org.babyfish.graphql.provider.meta.QueryProp
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Filterable
import java.lang.reflect.InvocationTargetException

class FilterExecutionContext(
    val prop: GraphQLProp,
    val env: DataFetchingEnvironment,
    val argumentsConverter: ArgumentsConverter,
    val query: Filterable<out Entity<*>, *>,
)

internal fun withFilterExecutionContext(
    ctx: FilterExecutionContext,
    block: () -> Unit
) {
    val oldContext = filterExecutionContextLocal.get()
    filterExecutionContextLocal.set(ctx)
    try {
        block()
    } catch (ex: InvocationTargetException) {
        if (ex.targetException !is NoReturnValue) {
            throw ex.targetException
        }
    } finally {
        if (oldContext !== null) {
            filterExecutionContextLocal.set(oldContext)
        } else {
            filterExecutionContextLocal.remove()
        }
    }
}

internal val filterExecutionContext
    get() = filterExecutionContextLocal.get() ?: error(
        "No FilterExecutionContext. wrapper functions of " +
            "Query.Runtime.queryReference, Query.Runtime.queryList, Query.Runtime.queryConnection, " +
            "EntityMapper.Runtime.filterList and EntityMapper.Runtime.filterConnection " +
            "cannot be invoked directly because they can only be invoked by the framework internally"
    )

private val filterExecutionContextLocal = ThreadLocal<FilterExecutionContext>()