package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.graphql.UserImplementationPropGraphQLDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class UserImplementationDSL internal constructor(
    private val prop: ModelPropImpl
) {
    fun graphql(block: UserImplementationPropGraphQLDSL.() -> Unit) {
        val dsl = UserImplementationPropGraphQLDSL()
        dsl.block()
        prop.setBatchSize(dsl.batchSize)
    }
}