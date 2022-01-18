package org.babyfish.graphql.provider.server.dsl.redis

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class RedisDependencyDSL<E: Immutable> internal constructor(
    private val dependency: EntityPropImpl.RedisDependencyImpl
): AbstractRedisDependencyDSL<E>() {

    override val dependencyMap: MutableMap<String, EntityPropImpl.RedisDependencyImpl>
        get() = dependency.dependencyMap
}