package org.babyfish.graphql.provider.server.dsl.association

import org.babyfish.graphql.provider.server.dsl.GraphQLProviderDSL
import org.babyfish.graphql.provider.server.dsl.db.ReferenceDatabaseDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ReferenceDSL internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun db(block: ReferenceDatabaseDSL.() -> Unit) {
        ReferenceDatabaseDSL(entityProp).block()
    }
}