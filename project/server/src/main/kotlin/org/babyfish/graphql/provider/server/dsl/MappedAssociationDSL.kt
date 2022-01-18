package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class MappedAssociationDSL<E: Immutable, T: Immutable> internal constructor(
    private val entityProp: EntityPropImpl
): AbstractAssociationDSL<E, T>(entityProp)