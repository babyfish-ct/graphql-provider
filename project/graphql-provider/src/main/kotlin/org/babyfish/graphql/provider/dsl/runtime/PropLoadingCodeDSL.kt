package org.babyfish.graphql.provider.dsl.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL
import org.babyfish.kimmer.sql.Entity

@GraphQLProviderDSL
class PropLoadingCodeDSL<E: Entity<ID>, ID: Comparable<ID>, R> internal constructor(): AbstractCodeDSL() {

    private var async: (suspend (E) -> R)? = null

    fun async(block: suspend (E) -> R) {
        if (async !== null) {
            throw ModelException("'PropLoadingCodeDSL.async { ... }' can only be specified once")
        }
        this.async = block
    }

    internal fun async(): suspend (E) -> R =
        async ?: throw ModelException("'PropLoadingCodeDSL.async { ... }' must be specified")
}