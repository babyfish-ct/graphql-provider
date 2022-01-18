package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class AssociationDSL<E: Immutable, T: Immutable> internal constructor(
    private val entityProp: EntityPropImpl
): AbstractAssociationDSL<E, T>(entityProp) {

    fun db(block: AssociationDatabaseDSL.() -> Unit) {
        AssociationDatabaseDSL(entityProp).block()
    }
}