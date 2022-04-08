package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.sql.meta.config.Storage

@GraphQLProviderDSL
class CollectionDSL internal constructor(
    prop: ModelPropImpl
): AbstractAssociationPropDSL(prop) {

    private var storage: Storage? = null

    fun db(block: AssociationDatabaseDSL.() -> Unit) {
        storage = AssociationDatabaseDSL(prop).run {
            block()
            storage()
        }
    }

    internal fun storage(): Storage =
        storage
            ?: throw ModelException("Middle table of '${prop}.db' is not specified")
}