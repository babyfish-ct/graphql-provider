package com.babyfish.graphql.provider.server.cfg

import com.babyfish.graphql.provider.server.cfg.model.AuthorAssembler
import com.babyfish.graphql.provider.server.cfg.model.BookAssembler
import com.babyfish.graphql.provider.server.cfg.model.BookRepository
import com.babyfish.graphql.provider.server.cfg.model.BookStoreAssembler
import org.babyfish.graphql.provider.server.runtime.EntityTypeProvider

import kotlin.test.Test

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


