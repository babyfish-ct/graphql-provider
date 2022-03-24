package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.graphql.EntityPropGraphQLDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl

@GraphQLProviderDSL
abstract class AbstractAssociationDSL internal constructor(
    internal val prop: ModelPropImpl
) {
    fun graphql(block: EntityPropGraphQLDSL.() -> Unit) {
        val dsl = EntityPropGraphQLDSL()
        dsl.block()
        prop.setHidden(dsl.hidden)
        prop.setBatchSize(dsl.batchSize)
    }
}