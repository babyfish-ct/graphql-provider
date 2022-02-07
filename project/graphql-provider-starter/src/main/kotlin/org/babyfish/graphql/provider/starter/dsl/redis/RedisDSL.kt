package org.babyfish.graphql.provider.starter.dsl.redis

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl
import kotlin.time.Duration

@GraphQLProviderDSL
class RedisDSL internal constructor(
    private val redis: EntityTypeImpl.RedisImpl
) {
    var enabled: Boolean by redis::enabled
    var timeout: Duration? by redis::timeout
    var nullTimeout: Duration? by redis::nullTimeout
}