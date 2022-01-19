package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.MetadataException
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
open class AssociationDatabaseDSL internal constructor(
    internal val entityProp: EntityPropImpl
) {

    fun middleTable(block: MiddleTableDSL.() -> Unit) {
        if (entityProp.column !== null) {
            throw MetadataException("Cannot configure middle table for '${entityProp}' because its foreign key has been configured")
        }
        if (!entityProp.isAssociation) {
            throw MetadataException("Cannot configure foreign key for '${entityProp}' because its category is not association")
        }
        val middleTable = entityProp.MiddleTableImpl().also {
            MiddleTableDSL(it).block()
        }
    }
}