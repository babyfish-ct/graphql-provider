package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.graphql.EntityPropGraphQLDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl

@GraphQLProviderDSL
abstract class AbstractAssociationPropDSL internal constructor(
    prop: ModelPropImpl
): AbstractPropDSL(prop) {

    fun graphql(block: EntityPropGraphQLDSL.() -> Unit) {
        val dsl = EntityPropGraphQLDSL()
        dsl.block()
        prop.setHidden(dsl.hidden)
        prop.setBatchSize(dsl.batchSize)
    }
}