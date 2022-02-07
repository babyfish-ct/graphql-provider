package org.babyfish.graphql.provider.starter.dsl.graphql

import org.babyfish.graphql.provider.starter.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityTypeImpl

@GraphQLProviderDSL
class EntityTypeGraphQLDSL internal constructor(
    private val graphql: EntityTypeImpl.GraphQLImpl
) {
    var defaultBatchSize: Int? by graphql::defaultBatchSize
    var defaultCollectionBatchSize: Int? by graphql::defaultCollectionBatchSize
}