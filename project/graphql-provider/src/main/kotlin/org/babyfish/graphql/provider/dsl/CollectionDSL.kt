package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.kimmer.sql.meta.config.Storage

@GraphQLProviderDSL
class CollectionDSL internal constructor(
    prop: ModelProp
): AbstractAssociationDSL(prop) {

    private var storage: Storage? = null

    fun db(block: AssociationDatabaseDSL.() -> Unit) {
        storage = AssociationDatabaseDSL(prop).run {
            block()
            create()
        }
    }

    internal fun create(): Storage =
        storage
            ?: throw ModelException("Middle table of '${prop}.db' is not specified")
}