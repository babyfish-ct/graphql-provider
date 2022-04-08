package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.security.SecurityDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl

@GraphQLProviderDSL
abstract class AbstractPropDSL internal constructor(
    internal val prop: ModelPropImpl
) {
    fun security(block: SecurityDSL.() -> Unit) {
        val dsl = SecurityDSL()
        dsl.block()
        prop.setSecurityPredicate(dsl.predicate())
    }
}