package org.babyfish.graphql.provider.starter.dsl.db

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl

@GraphQLProviderDSL
class EntityTypeDatabaseDSL internal constructor(
    private val database: EntityTypeImpl.DatabaseImpl
) {
    var tableName: String? by database::userTableName
}