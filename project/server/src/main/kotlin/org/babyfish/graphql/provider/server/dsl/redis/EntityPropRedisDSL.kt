package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropRedisDependencyImpl
import org.babyfish.graphql.provider.server.meta.impl.EntityPropRedisImpl
import kotlin.time.Duration

@GraphQLProviderDSL
class EntityPropRedisDSL<E> internal constructor(
    private val redis: EntityPropRedisImpl
): AbstractRedisDependencyDSL<E>() {

    var enabled: Boolean by redis::enabled

    var timeout: Duration? by redis::timeout

    val nullTimeout: Duration? by redis::nullTimeout

    override val dependencyMap: MutableMap<String, EntityPropRedisDependencyImpl>
        get() = redis.dependencyMap
}