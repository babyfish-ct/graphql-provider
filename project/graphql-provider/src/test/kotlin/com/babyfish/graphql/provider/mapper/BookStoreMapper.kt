package com.babyfish.graphql.provider.mapper

import com.babyfish.graphql.provider.model.Book
import com.babyfish.graphql.provider.model.BookStore
import com.babyfish.graphql.provider.model.name
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.kimmer.sql.ast.ilike
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookStoreMapper(
    private val bookRepository: BookRepository
): org.babyfish.graphql.provider.EntityMapper<BookStore, UUID>() {

    override fun EntityTypeDSL<BookStore, UUID>.config() {

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
                    where(table.name ilike it)
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

