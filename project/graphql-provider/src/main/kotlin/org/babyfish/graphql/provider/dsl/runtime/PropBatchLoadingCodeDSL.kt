package org.babyfish.graphql.provider.dsl.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL

@GraphQLProviderDSL
class PropBatchLoadingCodeDSL<ID: Comparable<ID>, R> internal constructor(): AbstractCodeDSL() {

    private var async: (suspend (Set<ID>) -> Map<ID, R>)? = null

    fun async(block: suspend (Set<ID>) -> Map<ID, R>) {
        if (async !== null) {
            throw ModelException("'PropBatchLoadingCodeDSL.async { ... }' can only be specified once")
        }
        this.async = block
    }

    internal fun async(): suspend (Set<ID>) -> Map<ID, R> =
        async ?: throw ModelException("'PropBatchLoadingCodeDSL.async { ... }' must be specified")
}