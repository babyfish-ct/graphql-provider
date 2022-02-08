package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable

internal class TypedQueryImpl<T: Immutable, R>(
    private val baseQuery: DatabaseQuery<T>
) : TypedDatabaseQuery<T, R>, DatabaseQuery<T> by (baseQuery), Renderable {

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("select * ")
        (baseQuery as Renderable).renderTo(builder)
    }
}