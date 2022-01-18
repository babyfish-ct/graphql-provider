package org.babyfish.graphql.provider.server

import org.babyfish.kimmer.Immutable
import org.babyfish.graphql.provider.server.cfg.EntityTypeConfiguration

interface EntityAssembler<E: Immutable> {

    fun EntityTypeConfiguration<E>.assemble()
}