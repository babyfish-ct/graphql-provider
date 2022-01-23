package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.redis.RedisDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class MappedCollectionDSL<E: Immutable> internal constructor(
    prop: EntityPropImpl
) {
    fun redis(block: RedisDSL.() -> Unit) {

    }
}