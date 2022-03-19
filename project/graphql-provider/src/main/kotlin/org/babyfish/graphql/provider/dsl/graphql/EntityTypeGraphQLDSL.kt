package org.babyfish.graphql.provider.dsl.graphql

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl

@GraphQLProviderDSL
class EntityTypeGraphQLDSL internal constructor(
    private var name: String
) {
    private var defaultBatchSize: Int? = null
    private var defaultCollectionBatchSize: Int? = null
    internal fun create(): ModelType.GraphQL =
        ModelType.GraphQL(
            name = name,
            defaultBatchSize = defaultBatchSize,
            defaultCollectionBatchSize = defaultCollectionBatchSize
        )
}