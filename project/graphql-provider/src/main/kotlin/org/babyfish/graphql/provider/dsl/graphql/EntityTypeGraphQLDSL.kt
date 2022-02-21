package org.babyfish.graphql.provider.dsl.graphql

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl

@GraphQLProviderDSL
class EntityTypeGraphQLDSL internal constructor() {
    var defaultBatchSize: Int? = null
    var defaultCollectionBatchSize: Int? = null
    internal fun create(): ModelType.GraphQL =
        ModelType.GraphQL(
            defaultBatchSize = defaultBatchSize,
            defaultCollectionBatchSize = defaultCollectionBatchSize
        )
}