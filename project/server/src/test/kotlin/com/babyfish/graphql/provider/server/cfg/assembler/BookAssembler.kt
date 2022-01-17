package com.babyfish.graphql.provider.server.cfg.assembler

import com.babyfish.graphql.provider.server.cfg.Book
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration
import org.babyfish.graphql.provider.server.cfg.db.precision
import org.babyfish.graphql.provider.server.cfg.db.scale

class BookAssembler: EntityAssembler<Book> {

    override fun EntityConfiguration<Book>.assemble() {

        id(Book::id)

        reference(Book::store) {
            db {
                foreignKey("STORE_ID")
            }
        }

        list(Book::authors) {
            db {
                middleTable(
                    "BOOK_AUTHOR_MAPPING",
                    "BOOK_ID",
                    "AUTHOR_ID"
                )
            }
        }

        scalar(Book::price) {
            db {
                precision(10)
                scale(2)
            }
        }
    }
}