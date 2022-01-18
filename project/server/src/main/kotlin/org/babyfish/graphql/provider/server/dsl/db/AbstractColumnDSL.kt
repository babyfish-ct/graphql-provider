package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
abstract class AbstractColumnDSL<T> internal constructor(
    internal val column: EntityPropImpl.ColumnImpl
) {
    var columnName: String? by column::userName
}

var AbstractColumnDSL<String>.length: Int?
    get() = column.length
    set(value) { column.length = value }

var AbstractColumnDSL<out Number>.precision: Int?
    get() = column.precision
    set(value) { column.precision = value }

var AbstractColumnDSL<out Number>.scale: Int?
    get() = column.scale
    set(value) { column.scale = value }
