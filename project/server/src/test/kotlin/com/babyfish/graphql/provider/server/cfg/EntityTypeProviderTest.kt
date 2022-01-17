package com.babyfish.graphql.provider.server.cfg

import com.babyfish.graphql.provider.server.cfg.assembler.AuthorAssembler
import com.babyfish.graphql.provider.server.cfg.assembler.BookAssembler
import com.babyfish.graphql.provider.server.cfg.assembler.BookStoreAssembler
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.EntityTypeProvider
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration
import org.babyfish.graphql.provider.server.cfg.db.length
import org.babyfish.graphql.provider.server.cfg.db.precision
import org.babyfish.graphql.provider.server.cfg.db.scale
import org.babyfish.graphql.provider.server.meta.Arg
import org.springframework.stereotype.Component
import kotlin.test.Test

class EntityTypeProviderTest {

    @Test
    fun test() {
        val entityTypeProvider = EntityTypeProvider(
            listOf(
                BookStoreAssembler(),
                BookAssembler(),
                AuthorAssembler()
            )
        )
    }
}
