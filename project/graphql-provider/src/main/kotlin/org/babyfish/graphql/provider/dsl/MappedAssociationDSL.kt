package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.meta.impl.ModelPropImpl

@GraphQLProviderDSL
class MappedAssociationDSL internal constructor(
    prop: ModelPropImpl
): AbstractAssociationPropDSL(prop)