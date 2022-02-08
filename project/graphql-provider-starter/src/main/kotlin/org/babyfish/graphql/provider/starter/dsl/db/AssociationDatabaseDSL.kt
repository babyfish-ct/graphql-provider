package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
open class AssociationDatabaseDSL internal constructor(
    internal val entityProp: EntityPropImpl
) {

    fun middleTable(block: MiddleTableDSL.() -> Unit) {
        if (entityProp.column !== null) {
            throw ModelException("Cannot configure middle table for '${entityProp}' because its foreign key has been configured")
        }
        if (entityProp.targetEntityType === null) {
            throw ModelException("Cannot configure foreign key for '${entityProp}' because its category is not association")
        }
        val middleTable = entityProp.MiddleTableImpl().also {
            MiddleTableDSL(it).block()
        }
    }
}