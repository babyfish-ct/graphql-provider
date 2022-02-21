package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.dsl.redis.CacheDSL
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.runtime.filterExecutionContext
import org.babyfish.graphql.provider.runtime.registerQueryField
import org.babyfish.graphql.provider.runtime.registerQueryFieldCache
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity

abstract class Query(
    redisBlock: (CacheDSL.() -> Unit)? = null
) {

    protected fun <N: Entity<NID>, NID: Comparable<NID>> queryConnection(
        redisBlock: (CacheDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<N, NID>.() -> Unit
    ): Connection<N> {
        query(redisBlock, filterBlock)
    }

    protected fun <E: Entity<EID>, EID: Comparable<EID>> queryList(
        redisBlock: (CacheDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<E, EID>.() -> Unit
    ): List<E> {
        query(redisBlock, filterBlock)
    }

    protected fun <R: Entity<RID>, RID: Comparable<RID>> queryReference(
        redisBlock: (CacheDSL.() -> Unit)? = null,
        filterBlock: FilterDSL<R, RID>.() -> Unit
    ): R {
        query(redisBlock, filterBlock)
    }

    private fun <E: Entity<ID>, ID: Comparable<ID>> query(
        redisBlock: (CacheDSL.() -> Unit)?,
        filterBlock: FilterDSL<E, ID>.() -> Unit
    ): Nothing {
        if (registerQueryField(this)) {
            if (redisBlock !== null) {
                val cache = CacheDSL().run {
                    redisBlock()
                    create()
                }
                registerQueryFieldCache(cache)
            }
            throw NoReturnValue()
        }
        FilterDSL<E, ID>(filterExecutionContext).filterBlock()
        throw NoReturnValue()
    }
}
