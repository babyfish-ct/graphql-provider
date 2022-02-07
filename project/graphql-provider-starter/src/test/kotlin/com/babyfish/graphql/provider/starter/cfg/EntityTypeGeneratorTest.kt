package com.babyfish.graphql.provider.starter.cfg

import com.babyfish.graphql.provider.starter.cfg.mapper.AuthorMapper
import com.babyfish.graphql.provider.starter.cfg.mapper.BookMapper
import com.babyfish.graphql.provider.starter.cfg.mapper.BookRepository
import com.babyfish.graphql.provider.starter.cfg.mapper.BookStoreMapper
import com.babyfish.graphql.provider.starter.cfg.query.AuthorQueryService
import com.babyfish.graphql.provider.starter.cfg.query.BookQueryService
import com.babyfish.graphql.provider.starter.cfg.query.BookStoreQueryService
import org.babyfish.graphql.provider.starter.runtime.EntityTypeGenerator

import kotlin.test.Test

class EntityTypeGeneratorTest {

    private val entityTypeGenerator =
        EntityTypeGenerator(
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


