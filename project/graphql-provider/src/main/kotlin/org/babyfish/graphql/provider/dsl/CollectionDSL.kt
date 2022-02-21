package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.db.AssociationDatabaseDSL
import org.babyfish.graphql.provider.meta.ModelProp

@GraphQLProviderDSL
class CollectionDSL internal constructor(
    modelProp: ModelProp
): AbstractAssociationDSL(modelProp) {

    fun db(block: AssociationDatabaseDSL.() -> Unit) {
        AssociationDatabaseDSL(modelProp).apply {
            block()
            validate()
        }
    }
}