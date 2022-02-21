package org.babyfish.graphql.provider.dsl.redis

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.Cache
import org.babyfish.graphql.provider.meta.CacheLevel
import kotlin.time.Duration

@GraphQLProviderDSL
class CacheDSL internal constructor() {
    var level: CacheLevel = CacheLevel.NO_CACHE
    var timeout: Duration? = null
    var nullTimeout: Duration? = null

    internal fun create(): Cache =
        Cache(
            level,
            timeout = timeout,
            nullTimeout = nullTimeout
        )
}