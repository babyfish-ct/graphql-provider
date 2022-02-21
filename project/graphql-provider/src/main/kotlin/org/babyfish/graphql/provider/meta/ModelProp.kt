package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.meta.EntityProp

interface ModelProp: GraphQLProp, EntityProp {

    override val name: String
        get() = super.name

    val cache: Cache

    val userImplementation: UserImplementation?

    val filter: Filter?

    override val arguments: List<Argument>
        get() = filter?.arguments ?: emptyList()
}

