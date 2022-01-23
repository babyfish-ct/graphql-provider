package org.babyfish.graphql.provider.server

import org.babyfish.graphql.provider.server.dsl.FilterDSL
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import java.lang.ref.Reference

abstract class QueryService {

    protected suspend fun <N: Immutable> queryConnection(
        block: FilterDSL<N>.() -> Unit
    ): Connection<N> =
        TODO()

    protected suspend fun <E: Immutable> queryList(
        block: FilterDSL<E>.() -> Unit
    ): List<E> =
        TODO()

    protected suspend fun <R: Immutable> queryReference(
        block: FilterDSL<R>.() -> Unit
    ): R =
        TODO()
}
