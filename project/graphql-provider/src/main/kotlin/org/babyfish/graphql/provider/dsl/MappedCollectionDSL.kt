package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class MappedCollectionDSL<E: Immutable> internal constructor(
    entityProp: ModelPropImpl
): AbstractAssociationDSL(entityProp) {
}