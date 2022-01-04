package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.meta.EntityPropImpl

class ScalarConfiguration<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: ColumnConfiguration<T>.() -> Unit) {
        ColumnConfiguration<T>(entityProp.column!!).block()
    }
}

class ColumnConfiguration<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnConfiguration<T>(column) {
    fun nullable(value: Boolean = true) {
        column.nullable = value
    }
}