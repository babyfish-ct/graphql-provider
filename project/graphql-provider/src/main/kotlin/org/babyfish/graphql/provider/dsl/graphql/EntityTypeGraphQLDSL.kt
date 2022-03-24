package org.babyfish.graphql.provider.dsl.graphql

import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.meta.ModelGraphQL
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl

@GraphQLProviderDSL
class EntityTypeGraphQLDSL internal constructor() {

    var name: String? = null

    var defaultBatchSize: Int? = null

    var defaultCollectionBatchSize: Int? = null

    internal fun graphql(defaultName: String): ModelGraphQL =
        ModelGraphQL(
            name = name ?: defaultName,
            defaultBatchSize = defaultBatchSize ?: 128,
            defaultCollectionBatchSize = defaultCollectionBatchSize ?: 16
        )
}