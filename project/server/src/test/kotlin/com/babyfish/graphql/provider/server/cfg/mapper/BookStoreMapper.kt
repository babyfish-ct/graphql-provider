package com.babyfish.graphql.provider.server.cfg.mapper

import com.babyfish.graphql.provider.server.cfg.Book
import com.babyfish.graphql.provider.server.cfg.BookStore
import org.babyfish.graphql.provider.server.EntityMapper
import org.babyfish.graphql.provider.server.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.server.runtime.expression.ilike
import org.springframework.stereotype.Component

@Component
class BookStoreMapper(
    private val bookRepository: BookRepository
): EntityMapper<BookStore>() {

    override fun EntityTypeDSL<BookStore>.map() {

        id(BookStore::id)

        mappedList(BookStore::books, Book::store)
    }

    fun books(name: String?) {
        filterList(BookStore::books) {
            name?.whenNotBlank {
                db {
                    where(table[Book::name] ilike it)
                }
                redis {
                    dependsOn(Book::name)
                }
            }
        }
    }

    fun avgPrice() {
        userImplementation(BookStore::avgPrice) {
            batch {
                bookRepository.findAvgPrices(rows.map { it.id })
            }
            redis {
                dependsOnList(BookStore::books) {
                    dependsOn(Book::name)
                }
            }
        }
    }
}

