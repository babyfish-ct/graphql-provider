package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.graphql.provider.starter.runtime.expression.Expression

internal class Order(
    private val expression: Expression<*>,
    private val descending: Boolean
): Renderable {

    override fun SqlBuilder.render() {
        (expression as Renderable).apply {
            render()
        }
        sql(" ")
        sql(if (descending) "desc" else "asc")
    }
}