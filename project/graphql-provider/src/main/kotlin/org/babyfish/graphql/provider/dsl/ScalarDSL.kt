package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.db.ScalarDatabaseDSL
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.Storage
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class ScalarDSL<E: Entity<ID>, ID: Comparable<ID>, T> internal constructor(
    private val prop: KProperty1<E, T?>
) {

    private var storage: Storage? = null

    fun db(block: ScalarDatabaseDSL<E, ID, T>.() -> Unit) {
        if (storage !== null) {
            throw ModelException("Cannot configure 'db {...}' for '${prop}' because its storage has been configured")
        }
        storage = ScalarDatabaseDSL<E, ID, T>(prop).run {
            block()
            create()
        }
    }

    internal fun create(): Storage? = storage
}
