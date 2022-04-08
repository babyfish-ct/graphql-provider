package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.graphql.UserImplementationPropGraphQLDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl

@GraphQLProviderDSL
class UserImplementationPropDSL internal constructor(
    prop: ModelPropImpl
): AbstractPropDSL(prop) {

    fun graphql(block: UserImplementationPropGraphQLDSL.() -> Unit) {
        val dsl = UserImplementationPropGraphQLDSL()
        dsl.block()
        prop.setBatchSize(dsl.batchSize)
    }
}