package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass

internal class SubQueryImpl<P: Immutable, T: Immutable>(
    private val parentQuery: AbstractQuery<P>,
    type: KClass<T>,
): AbstractQuery<T>(
    parentQuery.tableAliasAllocator,
    parentQuery.entityTypeMap,
    type
), DatabaseSubQuery<P, T> {

    override val parentTable: JoinableTable<P>
        get() = parentQuery.table
}