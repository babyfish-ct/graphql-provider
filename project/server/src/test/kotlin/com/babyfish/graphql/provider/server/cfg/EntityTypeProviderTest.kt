package com.babyfish.graphql.provider.server.cfg

import com.babyfish.graphql.provider.server.cfg.model.AuthorAssembler
import com.babyfish.graphql.provider.server.cfg.model.BookAssembler
import com.babyfish.graphql.provider.server.cfg.model.BookRepository
import com.babyfish.graphql.provider.server.cfg.model.BookStoreAssembler
import com.babyfish.graphql.provider.server.cfg.query.AuthorQueryService
import com.babyfish.graphql.provider.server.cfg.query.BookQueryService
import com.babyfish.graphql.provider.server.cfg.query.BookStoreQueryService
import graphql.schema.idl.SchemaPrinter
import org.babyfish.graphql.provider.server.runtime.EntityTypeProvider

import kotlin.test.Test

class EntityTypeProviderTest {

    private val entityTypeProvider =
        EntityTypeProvider(
            listOf(
                BookStoreQueryService(),
                BookQueryService(),
                AuthorQueryService()
            ),
            listOf(
                BookStoreAssembler(BookRepository()),
                BookAssembler(),
                AuthorAssembler()
            )
        )

    @Test
    fun testBookType() {
        println(SchemaPrinter().print(entityTypeProvider.schema))
    }
}


