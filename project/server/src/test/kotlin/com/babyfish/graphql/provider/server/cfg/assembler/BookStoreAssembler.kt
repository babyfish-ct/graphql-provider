package com.babyfish.graphql.provider.server.cfg.assembler

import com.babyfish.graphql.provider.server.cfg.Book
import com.babyfish.graphql.provider.server.cfg.BookStore
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.dsl.ArgumentType
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.runtime.ilike

class BookStoreAssembler(
    private val bookRepository: BookRepository
): EntityAssembler<BookStore> {

    override fun EntityTypeDSL<BookStore>.assemble() {

        id(BookStore::id)

        mappedList(BookStore::books, Book::store) {
            filter {
                argument(
                    "name",
                    ArgumentType.of(String::class).asNullable()
                ) {
                    where(table[Book::name] ilike it)
                }
            }
            redis {
                dependsOn(Book::name)
            }
        }

        computed(BookStore::avgPrice) {
            batchImplementation {
                bookRepository.findAvgPrices(rows.map { it.id })
            }
            redis {
                dependsOnList(BookStore::books) {
                    dependsOn(Book::price)
                }
            }
        }
    }
}

