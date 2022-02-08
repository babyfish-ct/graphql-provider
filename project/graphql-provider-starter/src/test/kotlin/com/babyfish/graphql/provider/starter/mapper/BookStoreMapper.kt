package com.babyfish.graphql.provider.starter.mapper

import com.babyfish.graphql.provider.starter.Book
import com.babyfish.graphql.provider.starter.BookStore
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.starter.runtime.query.ilike
import org.springframework.stereotype.Component

@Component
class BookStoreMapper(
    private val bookRepository: BookRepository
): EntityMapper<BookStore>() {

    override fun EntityTypeDSL<BookStore>.config() {

        id(BookStore::id)

        mappedList(BookStore::books, Book::store)

        userImplementation(BookStore::avgPrice) {
            batch {
                bookRepository.findAvgPrices(rows.map { it.id })
            }
        }
    }

    fun books(name: String?) {
        filterList(BookStore::books) {
            name?.let {
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
                dependsOn(BookStore::books)
                dependsOn(Book::name)
            }
        }
    }
}

