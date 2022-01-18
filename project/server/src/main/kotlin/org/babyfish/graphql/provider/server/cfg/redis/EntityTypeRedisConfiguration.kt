package org.babyfish.graphql.provider.server.cfg.redis

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl
import kotlin.time.Duration

@GraphQLProviderConfiguration
class EntityTypeRedisConfiguration internal constructor(
    private val graphql: EntityTypeImpl.RedisImpl
) {
    var timeout: Duration? by graphql::timeout
    var nullTimeout: Duration? by graphql::nullTimeout
}