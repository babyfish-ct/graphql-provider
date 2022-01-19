package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class CollectionDSL<E: Immutable> internal constructor(
    entityProp: EntityPropImpl
): ArgumentsDSL<E>(entityProp) {

    fun db(block: AssociationDatabaseDSL.() -> Unit) {

    }
}