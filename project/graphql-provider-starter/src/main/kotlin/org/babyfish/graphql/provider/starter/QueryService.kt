package org.babyfish.graphql.provider.starter

import org.babyfish.graphql.provider.starter.dsl.FilterDSL
import org.babyfish.graphql.provider.starter.dsl.redis.RedisDSL
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable

abstract class QueryService(
    redisBlock: (RedisDSL.() -> Unit)? = null
) {

    protected suspend fun <N: Immutable> queryConnection(
        redisBlock: (RedisDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<N>.() -> Unit
    ): Connection<N> =
        TODO()

    protected suspend fun <E: Immutable> queryList(
        redisBlock: (RedisDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<E>.() -> Unit
    ): List<E> =
        TODO()

    protected suspend fun <R: Immutable> queryReference(
        redisBlock: (RedisDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<R>.() -> Unit
    ): R =
        TODO()
}
