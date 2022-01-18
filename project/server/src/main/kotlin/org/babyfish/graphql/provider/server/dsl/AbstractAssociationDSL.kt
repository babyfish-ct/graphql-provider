package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.server.dsl.redis.EntityPropRedisDSL
import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
abstract class AbstractAssociationDSL<E: Immutable, T: Immutable> internal constructor(
    private val entityProp: EntityPropImpl
) {
    fun redis(block: EntityPropRedisDSL<T>.() -> Unit) {
        EntityPropRedisDSL<T>(entityProp.redis).block()
    }

    fun filter(vararg args: Arg, block: Filter<T>.() -> Unit) {

    }
}




