package com.babyfish.graphql.provider.mapper

import com.babyfish.graphql.provider.model.Author
import com.babyfish.graphql.provider.model.Book
import com.babyfish.graphql.provider.model.name
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.dsl.db.precision
import org.babyfish.graphql.provider.dsl.db.scale
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.meta.config.OnDeleteAction
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookMapper: org.babyfish.graphql.provider.EntityMapper<Book, UUID>() {

    override fun EntityTypeDSL<Book, UUID>.config() {

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
                column {
                    precision = 10
                    scale = 2
                }
            }
        }
    }

    fun authors(name: String?) {
        filterList(Book::authors) {
            name?.let {
                db {
                    where(table.name ilike it)
                }
                redis {
                    dependsOn(Author::name)
                }
            }
        }
    }
}
