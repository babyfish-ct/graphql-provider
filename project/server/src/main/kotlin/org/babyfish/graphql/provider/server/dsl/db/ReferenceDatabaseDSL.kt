package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.ModelException
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ReferenceDatabaseDSL internal constructor(
    entityProp: EntityPropImpl
): AssociationDatabaseDSL(entityProp) {

    fun foreignKey(block: ForeignKeyDSL.() -> Unit) {
        if (entityProp.middleTable !== null) {
            throw ModelException("Cannot configure foreign key for '${entityProp}' because its middle table has been configured")
        }
        if (!entityProp.isReference) {
            throw ModelException("Cannot configure foreign key for '${entityProp}' because its category is not reference")
        }
        entityProp.column = entityProp.ColumnImpl().also {
            ForeignKeyDSL(it).block()
        }
    }
}