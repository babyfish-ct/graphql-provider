package org.babyfish.graphql.provider.server.meta

import kotlin.time.Duration

interface EntityPropRedis {
    val enabled: Boolean
    val timeout: Duration?
    val nullTimeout: Duration?
    val dependencies: Collection<EntityPropRedisDependency>
}