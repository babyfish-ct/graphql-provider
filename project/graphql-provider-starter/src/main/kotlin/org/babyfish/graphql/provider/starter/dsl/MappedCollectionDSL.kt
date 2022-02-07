package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.redis.RedisDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class MappedCollectionDSL<E: Immutable> internal constructor(
    entityProp: EntityPropImpl
): AbstractAssociationDSL(entityProp) {
}