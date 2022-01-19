package com.babyfish.graphql.provider.server.cfg.assembler

import com.babyfish.graphql.provider.server.cfg.Author
import com.babyfish.graphql.provider.server.cfg.Book
import com.babyfish.graphql.provider.server.cfg.BookStore
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.dsl.ArgumentType
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.dsl.db.precision
import org.babyfish.graphql.provider.server.dsl.db.scale
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
import org.babyfish.graphql.provider.server.runtime.ilike

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
            filter {
                argument(
                    "name",
                    ArgumentType.of(String::class).asNullable()
                ) {
                    where(table[Author::name] ilike it)
                }
            }
            redis {
                dependsOn(Author::name)
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