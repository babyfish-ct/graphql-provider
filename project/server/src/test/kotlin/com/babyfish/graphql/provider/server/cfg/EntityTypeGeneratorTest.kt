package com.babyfish.graphql.provider.server.cfg

import com.babyfish.graphql.provider.server.cfg.model.AuthorMapper
import com.babyfish.graphql.provider.server.cfg.model.BookMapper
import com.babyfish.graphql.provider.server.cfg.model.BookRepository
import com.babyfish.graphql.provider.server.cfg.model.BookStoreMapper
import com.babyfish.graphql.provider.server.cfg.query.AuthorQueryService
import com.babyfish.graphql.provider.server.cfg.query.BookQueryService
import com.babyfish.graphql.provider.server.cfg.query.BookStoreQueryService
import org.babyfish.graphql.provider.server.runtime.EntityTypeGenerator

import kotlin.test.Test

class EntityTypeGeneratorTest {

    private val entityTypeGenerator =
        EntityTypeGenerator(
            listOf(
                BookStoreQueryService(),
                BookQueryService(),
                AuthorQueryService()
            ),
            listOf(
                BookStoreMapper(BookRepository()),
                BookMapper(),
                AuthorMapper()
            )
        )

    @Test
    fun testBookType() {

    }
}


