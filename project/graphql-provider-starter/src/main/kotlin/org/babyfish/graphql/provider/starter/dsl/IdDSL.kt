package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.db.IdColumnDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class IdDSL<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: IdColumnDSL<T>.() -> Unit) {
        IdColumnDSL<T>(entityProp.column!!).block()
    }
}
