package org.babyfish.graphql.provider.server.dsl.association

import org.babyfish.graphql.provider.server.dsl.ArgumentsDSL
import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.dsl.redis.EntityPropRedisDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
abstract class AbstractCollectionDSL<T: Immutable> internal constructor(
    entityProp: EntityPropImpl
): ArgumentsDSL<T>(entityProp) {

    fun redis(block: EntityPropRedisDSL<T>.() -> Unit) {
        EntityPropRedisDSL<T>(entityProp.redis).block()
    }
}