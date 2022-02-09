package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable

internal class TypedQueryImpl<T: Immutable, R>(
    private val expressions: List<Expression<*>>,
    base: DatabaseQuery<T>
) : TypedDatabaseQuery<T, R>, DatabaseQuery<T> by (base), Renderable {

    val baseQuery = AbstractQuery.form(base)

    override fun renderTo(builder: SqlBuilder) {
        builder.sql("select ")
        var sp: String? = null
        for (expression in expressions) {
            if (sp !== null) {
                builder.sql(sp)
            } else {
                sp = ", "
            }
            (expression as Renderable).renderTo(builder)
        }
        baseQuery.renderWithoutSelection(builder)
    }
}