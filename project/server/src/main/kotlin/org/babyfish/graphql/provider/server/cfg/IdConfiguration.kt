package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.meta.EntityPropImpl

@Configuration
class IdConfiguration<T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: IdColumnConfiguration<T>.() -> Unit) {
        IdColumnConfiguration<T>(entityProp.column!!).block()
    }
}

class IdColumnConfiguration<T> internal constructor(
    column: EntityPropImpl.ColumnImpl
) : AbstractColumnConfiguration<T>(column)