package org.babyfish.graphql.provider.example.mapper.entity

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.example.dal.BookRepository
import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.model.BookStore
import org.babyfish.kimmer.sql.meta.config.UUIDIdGenerator
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookStoreMapper: EntityMapper<BookStore, UUID>() {

    // Static mapping configuration--------------------------------

    override fun EntityTypeDSL<BookStore, UUID>.config() {

        db {
            idGenerator(UUIDIdGenerator())
        }

        mappedList(BookStore::books, Book::store)

        userImplementation(BookStore::avgPrice) {
            security {
                not {
                    anonymous()
                }
            }
        }
    }

    // Dynamic code configuration--------------------------------

    fun avgPrice() =
        runtime.batchImplement(BookStore::avgPrice) {
            spring(BookRepository::class)
                .findAvgPriceGroupByStoreIds(it)
        }
}