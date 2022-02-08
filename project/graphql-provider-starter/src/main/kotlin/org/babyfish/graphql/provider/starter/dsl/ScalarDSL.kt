package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.db.ColumnDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ScalarDSL<T> internal constructor(
    private val entityProp: EntityPropImpl
) {
    init {
        entityProp.column = entityProp.ColumnImpl()
    }

    fun db(block: ColumnDSL<T>.() -> Unit) {
        ColumnDSL<T>(entityProp.column!!).block()
    }
}

