package org.babyfish.graphql.provider.server

import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL

interface EntityAssembler<E: Immutable> {

    fun EntityTypeDSL<E>.assemble()
}