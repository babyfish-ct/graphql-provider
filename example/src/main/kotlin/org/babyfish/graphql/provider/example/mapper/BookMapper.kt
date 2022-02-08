package org.babyfish.graphql.provider.example.mapper

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.springframework.stereotype.Component

@Component
class BookMapper: EntityMapper<Book>() {

    override fun EntityTypeDSL<Book>.config() {

    }
}