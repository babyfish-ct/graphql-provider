package org.babyfish.graphql.provider.server

interface Connection<N> {

    val edges: List<Edge<N>>

    interface Edge<N> {
        val node: N
        val cursor: String
    }
}