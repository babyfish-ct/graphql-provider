package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import kotlin.time.Duration

@GraphQLProviderDSL
class EntityPropRedisDSL<E> internal constructor(
    private val redis: EntityPropImpl.RedisImpl
): AbstractRedisDependencyDSL<E>() {

    var enabled: Boolean by redis::enabled

    var timeout: Duration? by redis::timeout

    val nullTimeout: Duration? by redis::nullTimeout

    override val dependencyMap: MutableMap<String, EntityPropImpl.RedisDependencyImpl>
        get() = redis.dependencyMap
}