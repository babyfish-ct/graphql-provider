package org.babyfish.graphql.provider.dsl.input

import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.kimmer.sql.Entity

class AssociatedInputTypeDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    modelType: ModelType
): AbstractInputTypeDSL<E, ID>(modelType) {

    fun createAttachedObjects() {
        insertable = true
    }

    fun deleteDetachedObjects() {
        deleteable = true
    }
}