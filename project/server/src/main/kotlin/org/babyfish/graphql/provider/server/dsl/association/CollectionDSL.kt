package org.babyfish.graphql.provider.server.dsl.association

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.server.dsl.redis.EntityPropRedisDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class CollectionDSL<T: Immutable> internal constructor(
    entityProp: EntityPropImpl
): AbstractCollectionDSL<T>(entityProp) {

    fun db(block: AssociationDatabaseDSL.() -> Unit) {

    }
}