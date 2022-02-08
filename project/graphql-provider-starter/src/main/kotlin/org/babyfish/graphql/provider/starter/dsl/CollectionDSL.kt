package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.starter.dsl.db.ReferenceDatabaseDSL
import org.babyfish.graphql.provider.starter.dsl.redis.RedisDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class CollectionDSL<E: Immutable> internal constructor(
    entityProp: EntityPropImpl
): AbstractAssociationDSL(entityProp) {

    fun db(block: AssociationDatabaseDSL.() -> Unit) {
        AssociationDatabaseDSL(entityProp).apply {
            block()
            validate()
        }
    }
}