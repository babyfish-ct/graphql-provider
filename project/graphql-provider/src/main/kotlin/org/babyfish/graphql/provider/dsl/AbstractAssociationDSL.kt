package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.graphql.EntityPropGraphQLDSL
import org.babyfish.graphql.provider.meta.ModelProp

@GraphQLProviderDSL
abstract class AbstractAssociationDSL internal constructor(
    protected val modelProp: ModelProp
) {
    fun redis(block: EntityPropGraphQLDSL.() -> Unit) {

    }

    fun graphql(block: GraphQLProviderDSL.() -> Unit) {

    }
}