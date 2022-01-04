package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.meta.EntityPropImpl

@Configuration
abstract class AbstractColumnConfiguration<T> internal constructor(internal val column: EntityPropImpl.ColumnImpl) {
    fun column(columnName: String) {
        column.userName = columnName
    }
}

fun AbstractColumnConfiguration<out String>.length(length: Int) {
    column.length = length
}

fun AbstractColumnConfiguration<out Number>.precision(precision: Int) {
    column.precision = precision
}

fun AbstractColumnConfiguration<out Number>.scale(scale: Int) {
    column.scale = scale
}