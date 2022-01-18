package com.babyfish.graphql.provider.server.cfg.assembler

import com.babyfish.graphql.provider.server.cfg.Book
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.dsl.db.precision
import org.babyfish.graphql.provider.server.dsl.db.scale
import org.babyfish.graphql.provider.server.meta.OnDeleteAction

class BookAssembler: EntityAssembler<Book> {

    override fun EntityTypeDSL<Book>.assemble() {

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
}