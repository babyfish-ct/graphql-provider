package org.babyfish.graphql.provider.meta

data class ModelGraphQL(
    val name: String,
    val defaultBatchSize: Int?,
    val defaultCollectionBatchSize: Int?
)