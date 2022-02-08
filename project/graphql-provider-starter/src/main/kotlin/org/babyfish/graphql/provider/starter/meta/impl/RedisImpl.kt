package org.babyfish.graphql.provider.starter.meta.impl

import org.babyfish.graphql.provider.starter.meta.Redis
import kotlin.time.Duration

internal class RedisImpl: Redis {
    override var enabled: Boolean = true
    override var timeout: Duration? = null
    override var nullTimeout: Duration? = null
}