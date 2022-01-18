package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.db.IdColumnDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class IdDSL<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: IdColumnDSL<T>.() -> Unit) {
        IdColumnDSL<T>(entityProp.column!!).block()
    }
}
