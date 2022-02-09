package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

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

    override fun validate() {
        if (entityProp.column === null && entityProp.middleTable === null) {
            throw ModelException("Neither foreign key nor middle table of '${entityProp}.db' is specified")
        }
    }
}