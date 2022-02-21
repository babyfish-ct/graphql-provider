package org.babyfish.graphql.provider.meta

import kotlin.time.Duration

data class Cache(
    val level: CacheLevel,
    val timeout: Duration? = null,
    val nullTimeout: Duration? = null
)