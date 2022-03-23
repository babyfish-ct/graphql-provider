package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class UserImplementationDSL<E: Immutable, T> internal constructor(
    private val entityProp: ModelPropImpl
)