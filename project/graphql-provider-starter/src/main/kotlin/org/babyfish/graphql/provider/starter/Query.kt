package org.babyfish.graphql.provider.starter

import org.babyfish.graphql.provider.starter.dsl.FilterDSL
import org.babyfish.graphql.provider.starter.dsl.redis.RedisDSL
import org.babyfish.graphql.provider.starter.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.starter.meta.impl.RedisImpl
import org.babyfish.graphql.provider.starter.runtime.filterExecutionContext
import org.babyfish.graphql.provider.starter.runtime.registerQueryField
import org.babyfish.graphql.provider.starter.runtime.registerQueryFieldRedis
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.Immutable

abstract class Query(
    redisBlock: (RedisDSL.() -> Unit)? = null
) {

    protected fun <N: Immutable> queryConnection(
        redisBlock: (RedisDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<N>.() -> Unit
    ): Connection<N> {
        query(redisBlock, filterBlock)
    }

    protected fun <E: Immutable> queryList(
        redisBlock: (RedisDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<E>.() -> Unit
    ): List<E> {
        query(redisBlock, filterBlock)
    }

    protected fun <R: Immutable> queryReference(
        redisBlock: (RedisDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<R>.() -> Unit
    ): R {
        query(redisBlock, filterBlock)
    }

    private fun <T: Immutable> query(
        redisBlock: (RedisDSL.() -> Unit)?,
        filterBlock: FilterDSL<T>.() -> Unit
    ): Nothing {
        if (registerQueryField(this)) {
            if (redisBlock !== null) {
                RedisImpl().apply {
                    RedisDSL(this).redisBlock()
                    registerQueryFieldRedis(this)
                }
            }
            throw NoReturnValue()
        }
        FilterDSL<T>(filterExecutionContext).filterBlock()
        throw NoReturnValue()
    }
}
