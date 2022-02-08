package org.babyfish.graphql.provider.starter.runtime.query

internal class Order(
    private val expression: Expression<*>,
    private val descending: Boolean
): Renderable {

    override fun renderTo(builder: SqlBuilder) {
        (expression as Renderable).renderTo(builder)
        builder.apply {
            sql(" ")
            sql(if (descending) "desc" else "asc")
        }
    }
}