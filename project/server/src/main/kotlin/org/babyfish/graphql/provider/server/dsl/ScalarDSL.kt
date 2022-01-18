package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.db.ColumnDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

class ScalarDSL<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: ColumnDSL<T>.() -> Unit) {
        ColumnDSL<T>(entityProp.column!!).block()
    }
}

