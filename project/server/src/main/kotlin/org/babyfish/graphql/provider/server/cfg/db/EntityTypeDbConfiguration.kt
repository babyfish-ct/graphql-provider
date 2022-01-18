package org.babyfish.graphql.provider.server.cfg.db

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl

@GraphQLProviderConfiguration
class EntityTypeDbConfiguration internal constructor(
    private val database: EntityTypeImpl.DatabaseImpl
) {
    var tableName: String? by database::userTableName
}