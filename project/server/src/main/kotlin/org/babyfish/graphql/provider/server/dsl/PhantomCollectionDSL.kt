package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class PhantomCollectionDSL<E: Immutable> internal constructor(
    prop: EntityPropImpl
): ArgumentsDSL<E>(prop) {

    fun filter(block: FilterDSL<E>.() -> Unit) {

    }
}