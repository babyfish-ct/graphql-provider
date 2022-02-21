package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import java.math.BigDecimal

@GraphQLProviderDSL
class ColumnDSL<@Suppress("UNUSED") T> internal constructor(
    private val propName: String
) {
    var columnName: String? = null
    internal var _length: Int? = null
    internal var _precision: Int? = null
    internal var _scale: Int? = null
    internal fun create(): Column =
        Column(
            name = columnName ?: databaseIdentifier(propName),
            length = _length,
            precision = _precision,
            scale = _scale
        )
}

var ColumnDSL<String>.length: Int?
    get() = _length
    set(value) { _length = value }

var ColumnDSL<out Number>.precision: Int?
    get() = _precision
    set(value) {_precision = value }

var ColumnDSL<out Number>.scale: Int?
    get() = _scale
    set(value) { _scale = value }
