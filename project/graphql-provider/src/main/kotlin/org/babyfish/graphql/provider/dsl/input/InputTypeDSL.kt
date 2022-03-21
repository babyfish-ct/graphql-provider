package org.babyfish.graphql.provider.dsl.input

import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SaveOptionsDSL
import java.lang.IllegalStateException

class InputTypeDSL<E: Entity<ID>, ID: Comparable<ID>> internal constructor(
    modelType: ModelType
): AbstractInputTypeDSL<E, ID>(modelType) {

    init {
        insertable = true
    }

    fun insertOnly() {
        if (!insertable) {
            throw IllegalStateException("Cannot use both insertOnly() and updateOnly()")
        }
        updatable = false
    }

    fun updateOnly() {
        if (!updatable) {
            throw IllegalStateException("Cannot use both insertOnly() and updateOnly()")
        }
        insertable = false
    }
}