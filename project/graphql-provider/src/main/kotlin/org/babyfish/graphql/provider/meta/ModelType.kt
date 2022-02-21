package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.meta.EntityType

interface ModelType : GraphQLType, EntityType {

    val cache: Cache

    val graphql: GraphQL

    override val props: Map<String, ModelProp>

    data class GraphQL(
        val defaultBatchSize: Int?,
        val defaultCollectionBatchSize: Int?
    )
}

