package org.babyfish.graphql.provider.dsl.db

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.Storage
import org.babyfish.kimmer.sql.spi.databaseIdentifier

@GraphQLProviderDSL
class ReferenceDatabaseDSL internal constructor(
    prop: ModelProp
): AssociationDatabaseDSL(prop) {

    fun foreignKey(block: (ForeignKeyDSL.() -> Unit)? = null) {
        if (storage !== null) {
            throw ModelException("Cannot configure foreign key for '${prop}' because its database storage has been configured")
        }
        if (!prop.isReference) {
            throw ModelException("Cannot configure foreign key for '${prop}' because its category is not reference")
        }
        ForeignKeyDSL().let {
            if (block !== null) {
                it.block()
            }
            storage = it.create(
                "${databaseIdentifier(prop.name)}_ID"
            )
        }
    }

    override fun create(): Storage? =
        storage

}