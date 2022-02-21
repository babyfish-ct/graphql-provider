package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.db.ColumnDSL
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.spi.databaseIdentifier

@GraphQLProviderDSL
class ScalarDSL<T> internal constructor(
    private val propName: String
) {

    internal var column: Column? = null

    fun db(block: ColumnDSL<T>.() -> Unit) {
        column = ColumnDSL<T>(propName).run {
            block()
            create()
        }
    }
}

