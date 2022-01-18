package org.babyfish.graphql.provider.server.dsl.graphql

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityTypeImpl

@GraphQLProviderDSL
class EntityTypeGraphQLDSL internal constructor(
    private val graphql: EntityTypeImpl.GraphQLImpl
) {
    var defaultBatchSize: Int? by graphql::defaultBatchSize
    var defaultCollectionBatchSize: Int? by graphql::defaultCollectionBatchSize
}