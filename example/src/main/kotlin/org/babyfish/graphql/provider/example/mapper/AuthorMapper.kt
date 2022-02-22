package org.babyfish.graphql.provider.example.mapper

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.model.Author
import org.babyfish.graphql.provider.example.model.Book
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthorMapper: EntityMapper<Author, UUID>() {

    override fun EntityTypeDSL<Author, UUID>.config() {
        mappedList(Author::books, Book::authors)
    }
}