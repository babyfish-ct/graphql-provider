package org.babyfish.graphql.provider.dsl

import org.babyfish.graphql.provider.dsl.db.ReferenceDatabaseDSL
import org.babyfish.graphql.provider.meta.ModelProp

@GraphQLProviderDSL
class ReferenceDSL internal constructor(
    modelProp: ModelProp
): AbstractAssociationDSL(modelProp) {

    fun db(block: ReferenceDatabaseDSL.() -> Unit) {
        ReferenceDatabaseDSL(modelProp).apply {
            block()
            validate()
        }
    }
}