package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.meta.EntityType

interface ModelType : GraphQLType, EntityType {

    val cache: Cache

    val graphql: GraphQL

    override val idProp: ModelProp

    override val versionProp: ModelProp?

    override val declaredProps: Map<String, ModelProp>

    override val props: Map<String, ModelProp>

    override val backProps: Set<ModelProp>

    data class GraphQL(
        val name: String,
        val defaultBatchSize: Int?,
        val defaultCollectionBatchSize: Int?
    )
}

