package org.babyfish.graphql.provider.server

import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration

interface EntityAssembler<E: Immutable> {

    fun EntityConfiguration<E>.assemble()
}