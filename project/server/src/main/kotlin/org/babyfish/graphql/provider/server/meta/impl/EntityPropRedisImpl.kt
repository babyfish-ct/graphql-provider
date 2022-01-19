package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.meta.EntityPropRedis
import org.babyfish.graphql.provider.server.meta.EntityPropRedisDependency
import kotlin.time.Duration

internal class EntityPropRedisImpl: EntityPropRedis {

    var dependencyMap = mutableMapOf<String, EntityPropRedisDependencyImpl>()

    override var enabled: Boolean = true
    override var timeout: Duration? = null
    override var nullTimeout: Duration? = null
    override val dependencies: Collection<EntityPropRedisDependency>
        get() = dependencyMap.values
}
