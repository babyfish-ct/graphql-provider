package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.meta.EntityProp

interface ModelProp: GraphQLProp, EntityProp {

    override val name: String

    override val declaringType: ModelType

    val filter: Filter?

    val hidden: Boolean

    val batchSize: Int?

    override val arguments: List<Argument>
        get() = userImplementation?.arguments ?: filter?.arguments ?: emptyList()
}

