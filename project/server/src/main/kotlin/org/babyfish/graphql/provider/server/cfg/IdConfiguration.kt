package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.cfg.db.IdColumnConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderConfiguration
class IdConfiguration<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: IdColumnConfiguration<T>.() -> Unit) {
        IdColumnConfiguration<T>(entityProp.column!!).block()
    }
}
