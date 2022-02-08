package com.babyfish.graphql.provider.starter.query

import com.babyfish.graphql.provider.starter.Book
import com.babyfish.graphql.provider.starter.BookSortedField
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.runtime.query.ge
import org.babyfish.graphql.provider.starter.runtime.query.ilike
import org.babyfish.graphql.provider.starter.runtime.query.le
import org.babyfish.kimmer.graphql.Connection
import java.math.BigDecimal
import kotlin.reflect.KProperty1

class BookQuery: Query() {

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
