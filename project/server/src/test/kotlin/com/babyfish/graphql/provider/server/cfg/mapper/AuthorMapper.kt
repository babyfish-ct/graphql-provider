package com.babyfish.graphql.provider.server.cfg.mapper

import com.babyfish.graphql.provider.server.cfg.Author
import com.babyfish.graphql.provider.server.cfg.Book
import org.babyfish.graphql.provider.server.EntityMapper
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.runtime.expression.ilike
import org.springframework.stereotype.Component

@Component
class AuthorMapper: EntityMapper<Author>() {

    override fun EntityTypeDSL<Author>.map() {

        id(Author::id)

        mappedList(Author::books, Book::authors)
    }

    fun books(name: String?) {
        filterList(Author::books) {
            name?.whenNotBlank {
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