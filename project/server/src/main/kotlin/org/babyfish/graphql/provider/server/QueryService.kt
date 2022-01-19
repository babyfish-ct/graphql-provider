package org.babyfish.graphql.provider.server

import org.babyfish.graphql.provider.server.dsl.FilterDSL
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable

abstract class QueryService {

    protected suspend fun <N: Immutable> buildConnection(
        blocK: FilterDSL<N>.() -> Unit
    ): Connection<N> =
        TODO()
}
