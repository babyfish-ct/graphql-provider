package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import kotlin.time.Duration

@GraphQLProviderDSL
class EntityTypeRedisDSL internal constructor(
    private val redis: EntityTypeImpl.RedisImpl
) {
    var enabled: Boolean by redis::enabled
    var timeout: Duration? by redis::timeout
    var nullTimeout: Duration? by redis::nullTimeout
}