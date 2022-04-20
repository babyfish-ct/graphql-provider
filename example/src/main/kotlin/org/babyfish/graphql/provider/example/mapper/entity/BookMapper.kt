package org.babyfish.graphql.provider.example.mapper.entity

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.model.firstName
import org.babyfish.graphql.provider.example.model.lastName
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.meta.config.UUIDIdGenerator
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookMapper: EntityMapper<Book, UUID>() {

    // Static mapping configuration--------------------------------

    override fun EntityTypeDSL<Book, UUID>.config() {

        db {
            idGenerator(UUIDIdGenerator())
        }

        reference(Book::store)

        list(Book::authors) {
            db {
                middleTable {
                    tableName = "BOOK_AUTHOR_MAPPING"
                    joinColumnName = "BOOK_ID"
                    targetJoinColumnName = "AUTHOR_ID"
                }
            }
        }

        list(Book::fans) {
            db {
                middleTable {
                    tableName = "FAVOURITE_BOOK_MAPPING"
                    joinColumnName = "BOOK_ID"
                    targetJoinColumnName = "APP_USER_ID"
                }
            }
        }
    }

    // Dynamic code configuration--------------------------------

    fun authors(firstName: String?, lastName: String?) =
        runtime.filterList(Book::authors) {
            db {
                firstName?.let {
                    where { table.firstName ilike it }
                }
                lastName?.let {
                    where { table.lastName ilike it }
                }
            }
        }
}