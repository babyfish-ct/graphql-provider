package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.babyfish.kimmer.sql.meta.config.Storage

@GraphQLProviderDSL
open class AssociationDatabaseDSL internal constructor(
    protected val prop: ModelProp
) {

    internal var storage: Storage? = null

    fun middleTable(block: MiddleTableDSL.() -> Unit) {
        if (storage !== null) {
            throw ModelException("Cannot configure middle table for '${prop}' because its database storage has been configured")
        }
        storage = MiddleTableDSL(prop).run {
            block()
            create()
        }
    }

    internal open fun create(): Storage? =
        storage
}