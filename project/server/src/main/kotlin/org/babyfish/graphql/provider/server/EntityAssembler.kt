package org.babyfish.graphql.provider.server

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration

interface EntityAssembler<E: Immutable> {

    fun assemble(configuration: EntityConfiguration<E>)
}