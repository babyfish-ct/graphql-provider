package com.babyfish.graphql.provider.server.cfg.model

import com.babyfish.graphql.provider.server.cfg.Author
import com.babyfish.graphql.provider.server.cfg.Book
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.runtime.ilike
import org.springframework.stereotype.Component

@Component
class AuthorAssembler: EntityAssembler<Author> {

    override fun EntityTypeDSL<Author>.assemble() {

        id(Author::id)

        mappedList(Author::books, Book::authors) {

            optionalArgument(
                "name",
                String::class
            ) {

                where(table[Book::name] ilike it)
                redis {
                    dependsOn(Book::name)
                }
            }
        }
    }
}