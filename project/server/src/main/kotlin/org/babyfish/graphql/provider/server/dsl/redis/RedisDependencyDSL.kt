package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropRedisDependencyImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class RedisDependencyDSL<E: Immutable> internal constructor(
    private val dependency: EntityPropRedisDependencyImpl
): AbstractRedisDependencyDSL<E>() {

    override val dependencyMap: MutableMap<String, EntityPropRedisDependencyImpl>
        get() = dependency.dependencyMap
}