package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderConfiguration
abstract class AbstractColumnConfiguration<T> internal constructor(
    internal val column: EntityPropImpl.ColumnImpl
) {
    var columnName: String? by column::userName
}

var AbstractColumnConfiguration<String>.length: Int?
    get() = column.length
    set(value) { column.length = value }

var AbstractColumnConfiguration<out Number>.precision: Int?
    get() = column.precision
    set(value) { column.precision = value }

var AbstractColumnConfiguration<out Number>.scale: Int?
    get() = column.scale
    set(value) { column.scale = value }
