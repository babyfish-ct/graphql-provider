package org.babyfish.graphql.provider.server.dsl.db

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl

@GraphQLProviderDSL
class EntityTypeDatabaseDSL internal constructor(
    private val database: EntityTypeImpl.DatabaseImpl
) {
    var tableName: String? by database::userTableName
}