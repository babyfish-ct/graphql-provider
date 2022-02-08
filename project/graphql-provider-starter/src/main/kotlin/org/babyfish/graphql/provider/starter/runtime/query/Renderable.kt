package org.babyfish.graphql.provider.starter.runtime.query

internal interface Renderable {
    fun renderTo(builder: SqlBuilder)
}