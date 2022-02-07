package com.babyfish.graphql.provider.starter.cfg.mapper

import com.babyfish.graphql.provider.starter.cfg.Author
import com.babyfish.graphql.provider.starter.cfg.Book
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.starter.runtime.expression.ilike
import org.springframework.stereotype.Component

@Component
class AuthorMapper: EntityMapper<Author>() {

    override fun EntityTypeDSL<Author>.config() {

        id(Author::id)

        mappedList(Author::books, Book::authors)
    }

    fun books(name: String?) {
        filterList(Author::books) {
            name?.let {
                db {
                    where(table[Book::name] ilike it)
                }
                redis {
                    dependsOn(Book::name)
                }
            }
        }
    }
}