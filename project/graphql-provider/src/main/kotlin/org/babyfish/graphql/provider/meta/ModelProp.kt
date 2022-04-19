package org.babyfish.graphql.provider.meta

import org.babyfish.kimmer.sql.meta.EntityProp

interface ModelProp: GraphQLProp, EntityProp {

    override val name: String

    override val declaringType: ModelType

    val filter: Filter?

    val userImplementation: UserImplementation?

    val securityPredicate: SecurityPredicate?

    val hidden: Boolean

    val batchSize: Int?
}

