package com.babyfish.graphql.provider.mapper.entity

import com.babyfish.graphql.provider.model.Author
import com.babyfish.graphql.provider.model.Book
import com.babyfish.graphql.provider.model.name
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.kimmer.sql.ast.ilike
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthorMapper: org.babyfish.graphql.provider.EntityMapper<Author, UUID>() {

    override fun EntityTypeDSL<Author, UUID>.config() {
        mappedList(Author::books, Book::authors)
    }

    fun books(name: String?) {
        filterList(Author::books) {
            name?.let {
                db {
                    where(table.name ilike it)
                }
                redis {
                    dependsOn(Book::name)
                }
            }
        }
    }
}