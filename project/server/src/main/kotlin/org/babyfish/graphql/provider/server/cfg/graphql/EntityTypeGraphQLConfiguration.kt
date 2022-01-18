package org.babyfish.graphql.provider.server.cfg.graphql

import org.babyfish.graphql.provider.server.cfg.GraphQLProviderConfiguration
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl

@GraphQLProviderConfiguration
class EntityTypeGraphQLConfiguration internal constructor(
    private val graphql: EntityTypeImpl.GraphQLImpl
) {
    var defaultBatchSize: Int? by graphql::defaultBatchSize
    var defaultCollectionBatchSize: Int? by graphql::defaultCollectionBatchSize
}