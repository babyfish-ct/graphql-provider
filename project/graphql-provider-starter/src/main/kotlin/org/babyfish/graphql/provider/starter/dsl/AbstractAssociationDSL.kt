package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.graphql.EntityPropGraphQLDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
abstract class AbstractAssociationDSL internal constructor(
    protected val entityProp: EntityPropImpl
) {
    fun redis(block: EntityPropGraphQLDSL.() -> Unit) {

    }

    fun graphql(block: GraphQLProviderDSL.() -> Unit) {

    }
}