package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.cfg.db.ColumnConfiguration
import org.babyfish.graphql.provider.server.meta.EntityPropImpl

class ScalarConfiguration<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: ColumnConfiguration<T>.() -> Unit) {
        ColumnConfiguration<T>(entityProp.column!!).block()
    }
}

