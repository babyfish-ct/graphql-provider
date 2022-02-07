package org.babyfish.graphql.provider.starter.dsl

import org.babyfish.graphql.provider.starter.dsl.db.ReferenceDatabaseDSL
import org.babyfish.graphql.provider.starter.dsl.graphql.EntityPropGraphQLDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl

@GraphQLProviderDSL
class ReferenceDSL internal constructor(
    entityProp: EntityPropImpl
): AbstractAssociationDSL(entityProp) {

    fun db(block: ReferenceDatabaseDSL.() -> Unit) {
        ReferenceDatabaseDSL(entityProp).block()
    }
}