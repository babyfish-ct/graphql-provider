package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.MetadataException
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderConfiguration
class AssociationDbConfiguration internal constructor(private val entityProp: EntityPropImpl) {

    fun foreignKey(block: ForeignKeyConfiguration.() -> Unit) {
        if (entityProp.middleTable !== null) {
            throw MetadataException("Cannot configure foreign key for '${entityProp}' because its middle table has been configured")
        }
        if (entityProp.category !== EntityProp.Category.REFERENCE) {
            throw MetadataException("Cannot configure foreign key for '${entityProp}' because its category is not reference")
        }
        entityProp.column = entityProp.ColumnImpl().also {
            ForeignKeyConfiguration(it).block()
        }
    }

    fun middleTable(block: MiddleTableConfiguration.() -> Unit) {
        if (entityProp.column !== null) {
            throw MetadataException("Cannot configure middle table for '${entityProp}' because its foreign key has been configured")
        }
        if (!entityProp.isAssociation) {
            throw MetadataException("Cannot configure foreign key for '${entityProp}' because its category is not association")
        }
        val middleTable = entityProp.MiddleTableImpl().also {
            MiddleTableConfiguration(it).block()
        }
    }
}