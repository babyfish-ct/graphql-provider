package org.babyfish.graphql.provider.dsl.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.GraphQLProviderDSL

@GraphQLProviderDSL
class CodeDSL<R> internal constructor(): AbstractCodeDSL() {

    private var async: (suspend () -> R)? = null

    fun async(block: suspend () -> R) {
        if (async !== null) {
            throw ModelException("'CodeDSL.async { ... }' can only be specified once")
        }
        this.async = block
    }

    internal fun async(): suspend () -> R =
        async ?: throw ModelException("'CodeDSL.async { ... }' must be specified")
}