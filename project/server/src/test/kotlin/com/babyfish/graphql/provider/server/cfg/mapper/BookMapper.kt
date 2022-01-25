package com.babyfish.graphql.provider.server.cfg.mapper

import com.babyfish.graphql.provider.server.cfg.Author
import com.babyfish.graphql.provider.server.cfg.Book
import org.babyfish.graphql.provider.server.EntityMapper
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.dsl.db.precision
import org.babyfish.graphql.provider.server.dsl.db.scale
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
import org.babyfish.graphql.provider.server.runtime.expression.ilike
import org.springframework.stereotype.Component

@Component
class BookMapper: EntityMapper<Book>() {

    override fun EntityTypeDSL<Book>.map() {

        id(Book::id)

        reference(Book::store) {
            db {
                foreignKey {
                    columnName = "STORE_ID"
                    onDelete = OnDeleteAction.CASCADE
                }
            }
        }

        list(Book::authors) {
            db {
                middleTable {
                    tableName = "BOOK_AUTHOR_MAPPING"
                    joinColumnName = "BOOK_ID"
                    targetJoinColumnName = "AUTHOR_ID"
                }
            }
        }

        scalar(Book::price) {
            db {
                precision = 10
                scale = 2
            }
        }
    }

    fun authors(name: String?) {
        filterList(Book::authors) {
            name?.whenNotBlank {
                db {
                    where(table[Author::name] ilike it)
                }
                redis {
                    dependsOn(Author::name)
                }
            }
        }
    }
}
