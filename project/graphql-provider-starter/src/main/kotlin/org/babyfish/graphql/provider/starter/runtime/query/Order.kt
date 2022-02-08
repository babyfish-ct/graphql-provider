package org.babyfish.graphql.provider.starter.runtime.query

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