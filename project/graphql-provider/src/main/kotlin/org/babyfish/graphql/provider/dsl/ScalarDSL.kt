package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.db.ScalarDatabaseDSL
import org.babyfish.graphql.provider.dsl.graphql.EntityPropGraphQLDSL
import org.babyfish.graphql.provider.dsl.graphql.ScalarPropGraphQLDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.Storage

@GraphQLProviderDSL
class ScalarDSL<E: Entity<ID>, ID: Comparable<ID>, T> internal constructor(
    prop: ModelPropImpl
): AbstractPropDSL(prop) {

    private var storage: Storage? = null

    fun db(block: ScalarDatabaseDSL<E, ID, T>.() -> Unit) {
        if (storage !== null) {
            throw ModelException("Cannot configure 'db {...}' for '${prop}' because its storage has been configured")
        }
        storage = ScalarDatabaseDSL<E, ID, T>(prop).run {
            block()
            storage()
        }
    }

    fun graphql(block: ScalarPropGraphQLDSL.() -> Unit) {
        val dsl = ScalarPropGraphQLDSL()
        dsl.block()
        prop.setHidden(dsl.hidden)
    }

    internal fun storage(): Storage? = storage
}
