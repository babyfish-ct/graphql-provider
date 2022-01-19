package com.babyfish.graphql.provider.server.cfg

import com.babyfish.graphql.provider.server.cfg.assembler.AuthorAssembler
import com.babyfish.graphql.provider.server.cfg.assembler.BookAssembler
import com.babyfish.graphql.provider.server.cfg.assembler.BookRepository
import com.babyfish.graphql.provider.server.cfg.assembler.BookStoreAssembler
import org.babyfish.graphql.provider.server.EntityTypeProvider
import org.babyfish.graphql.provider.server.meta.EntityType

import kotlin.test.Test
import kotlin.test.expect

class EntityTypeProviderTest {

    private val entityTypeProvider =
        EntityTypeProvider(
            listOf(
                BookStoreAssembler(BookRepository()),
                BookAssembler(),
                AuthorAssembler()
            )
        )

    @Test
    fun testBookType() {

    }
}


