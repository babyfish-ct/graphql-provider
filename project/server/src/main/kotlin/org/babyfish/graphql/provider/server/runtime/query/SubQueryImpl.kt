package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.runtime.DatabaseSubQuery
import org.babyfish.graphql.provider.server.runtime.JoinableTable
import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass

internal class SubQueryImpl<P: Immutable, T: Immutable, R>(
    private val parentQuery: AbstractQuery<P>,
    type: KClass<T>,
    private val fieldExpressionSupplier: ((JoinableTable<T>) -> Expression<R>)?
): AbstractQuery<T>(
    parentQuery.tableAliasAllocator,
    parentQuery.entityTypeMap,
    type
), DatabaseSubQuery<P, T, R> {

    override val parentTable: JoinableTable<P>
        get() = parentQuery.table

    override fun SqlBuilder.render() {
        val supplier = fieldExpressionSupplier
        sql("(")
        sql("select ")
        if (supplier === null) {
            sql("1")
        } else {
            supplier(table).apply {
                render()
            }
        }
        renderWithoutSelection()
        sql(")")
    }
}