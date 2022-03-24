package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.db.ReferenceDatabaseDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.spi.databaseIdentifier

@GraphQLProviderDSL
class ReferenceDSL internal constructor(
    prop: ModelPropImpl
): AbstractAssociationDSL(prop) {

    private var storage: Storage? = null

    fun db(block: ReferenceDatabaseDSL.() -> Unit) {
        ReferenceDatabaseDSL(prop).run {
            block()
            storage()
        }
    }

    internal fun storage(): Storage =
        storage ?: Column(name = "${databaseIdentifier(prop.name)}_ID")
}