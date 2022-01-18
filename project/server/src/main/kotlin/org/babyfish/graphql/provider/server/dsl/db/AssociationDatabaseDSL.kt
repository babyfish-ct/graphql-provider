package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.EntityPropCategory
import org.babyfish.graphql.provider.server.meta.MetadataException
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class AssociationDatabaseDSL internal constructor(private val entityProp: EntityPropImpl) {

    fun foreignKey(block: ForeignKeyDSL.() -> Unit) {
        if (entityProp.middleTable !== null) {
            throw MetadataException("Cannot configure foreign key for '${entityProp}' because its middle table has been configured")
        }
        if (entityProp.category !== EntityPropCategory.REFERENCE) {
            throw MetadataException("Cannot configure foreign key for '${entityProp}' because its category is not reference")
        }
        entityProp.column = entityProp.ColumnImpl().also {
            ForeignKeyDSL(it).block()
        }
    }

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