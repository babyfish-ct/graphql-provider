package com.babyfish.graphql.provider.starter.cfg.mapper

import com.babyfish.graphql.provider.starter.cfg.Author
import com.babyfish.graphql.provider.starter.cfg.Book
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.starter.dsl.db.precision
import org.babyfish.graphql.provider.starter.dsl.db.scale
import org.babyfish.graphql.provider.starter.meta.OnDeleteAction
import org.babyfish.graphql.provider.starter.runtime.expression.ilike
import org.springframework.stereotype.Component

@Component
class BookMapper: EntityMapper<Book>() {

    override fun EntityTypeDSL<Book>.config() {

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
            name?.let {
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
