package org.babyfish.graphql.provider.example.mapper.entity

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.model.*
import org.babyfish.kimmer.sql.ast.concat
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.meta.config.UUIDIdGenerator
import org.springframework.stereotype.Component
import java.util.*

@Component
class AuthorMapper: EntityMapper<Author, UUID>() {

    override fun EntityTypeDSL<Author, UUID>.config() {

        db {
            idGenerator(UUIDIdGenerator())
        }

        mappedList(Author::books, Book::authors)

        scalar(Author::fullName) {
            db {
                formula {
                    concat(firstName, value(" "), lastName)
                }
            }
        }
    }

    fun books(name: String?) =
        runtime.filterList(Author::books) {
            name?.let {
                db {
                    where {
                        table.id valueIn subQuery(Book::class) {
                            where(table.name ilike it)
                            select(table.authors.id)
                        }
                    }
                }
            }
        }
}