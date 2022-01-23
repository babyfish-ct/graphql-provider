package com.babyfish.graphql.provider.server.cfg.query

import com.babyfish.graphql.provider.server.cfg.Book
import com.babyfish.graphql.provider.server.cfg.BookSortedField
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.runtime.ge
import org.babyfish.graphql.provider.server.runtime.ilike
import org.babyfish.graphql.provider.server.runtime.le
import org.babyfish.kimmer.Connection
import java.math.BigDecimal
import kotlin.reflect.KProperty1

class BookQueryService: QueryService() {

    suspend fun findBooks(
        name: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        sortedField: BookSortedField = BookSortedField.NAME,
        descending: Boolean = false
    ): Connection<Book> = queryConnection {

        name?.let {
            db {
                where(table[Book::name] ilike it)
            }
            redis {
                dependsOn(Book::name)
            }
        }

        minPrice?.let {
            db {
                where(table[Book::price] ge it)
            }
            redis {
                dependsOn(Book::price)
            }
        }

        maxPrice?.let {
            db {
                where(table[Book::price] le it)
            }
            redis {
                dependsOn(Book::price)
            }
        }

        val prop: KProperty1<Book, *> = when (sortedField) {
            BookSortedField.NAME -> Book::name
            BookSortedField.PRICE -> Book::price
        }
        db {
            orderBy(prop, descending)
        }
        redis {
            dependsOn(prop)
        }
    }
}