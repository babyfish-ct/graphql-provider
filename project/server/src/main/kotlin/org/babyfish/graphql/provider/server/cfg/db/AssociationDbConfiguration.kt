package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.EntityPropImpl
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
import java.lang.IllegalStateException

@GraphQLProviderConfiguration
class AssociationDbConfiguration internal constructor(private val entityProp: EntityPropImpl) {

    fun foreignKey(
        columnName: String,
        onDelete: OnDeleteAction = OnDeleteAction.NONE
    ) {
        if (entityProp.middleTable !== null) {
            throw IllegalStateException("Cannot configure foreign key for '${entityProp.kotlinProp.name}' because its middle table has been configured")
        }
        if (entityProp.category !== EntityProp.Category.REFERENCE) {
            throw IllegalStateException("Cannot configure foreign key for '${entityProp.kotlinProp.name}' because its category is not ${EntityProp.Category.REFERENCE}")
        }
        val column = entityProp.ColumnImpl()
        column.userName = columnName
        column.onDelete = onDelete
        entityProp.column = column
    }

    fun middleTable(
        tableName: String,
        joinColumn: String,
        targetJoinColumn: String
    ) {
        if (entityProp.column !== null) {
            throw IllegalStateException("Cannot configure middle table for '${entityProp.kotlinProp.name}' because its foreign key has been configured")
        }
        val middleTable = entityProp.MiddleTableImpl()

    }
}