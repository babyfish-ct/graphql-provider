package org.babyfish.graphql.provider.server.runtime.query

internal interface Renderable {
    fun SqlBuilder.render()
}