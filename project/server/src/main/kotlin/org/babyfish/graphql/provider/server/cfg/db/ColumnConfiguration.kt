package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.meta.EntityPropImpl

class ColumnConfiguration<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnConfiguration<T>(column) {
    fun nullable(value: Boolean = true) {
        column.nullable = value
    }
}