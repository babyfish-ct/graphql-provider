package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.ModelException
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.table.NonNullTable
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Formula
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.spi.databaseIdentifier
import kotlin.reflect.KProperty1

class ScalarDatabaseDSL<E: Entity<ID>, ID: Comparable<ID>, T> internal constructor(
    private val prop: KProperty1<*, *>
) {

    private var storage: Storage? = null

    fun column(block: ColumnDSL<T>.() -> Unit) {
        if (storage !== null) {
            throw ModelException(
                "Cannot configure the column of '${prop}' " +
                    "because its storage has already been configured"
            )
        }
        storage = ColumnDSL<T>(prop.name).let {
            it.block()
            it.create()
        }
    }

    fun formula(block: NonNullTable<E, ID>.() -> Expression<T>) {
        if (storage !== null) {
            throw ModelException(
                "Cannot configure the formula of '${prop}' " +
                    "because its storage has already been configured"
            )
        }
        // TODO: Formula's T cannot be Any
        storage = Formula.of(block as (NonNullTable<E, ID>.() -> Expression<Any>))
    }

    internal fun create(): Storage {
        return storage ?: Column(name = databaseIdentifier(prop.name))
    }
}