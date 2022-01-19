package org.babyfish.graphql.provider.server

import org.babyfish.graphql.provider.server.dsl.QueryDSL

interface QueryAssembler<Q: AbstractQuery> {

    fun QueryDSL<Q>.assemble()
}