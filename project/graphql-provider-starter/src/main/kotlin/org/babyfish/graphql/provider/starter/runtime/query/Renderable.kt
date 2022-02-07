package org.babyfish.graphql.provider.starter.runtime.query

internal interface Renderable {
    fun SqlBuilder.render()
}