package com.babyfish.graphql.provider.query

import com.babyfish.graphql.provider.model.Book
import com.babyfish.graphql.provider.model.name
import com.babyfish.graphql.provider.model.price
import org.babyfish.graphql.provider.Query
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.ast.ge
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.le
import java.math.BigDecimal
import kotlin.reflect.KProperty1

class BookQuery: Query() {

    suspend fun findBooks(
        name: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        descending: Boolean = false
    ): Connection<Book> = queryConnection {

        name?.let {
            db {
                where(table.name ilike it)
            }
            redis {
                dependsOn(Book::name)
            }
        }

        minPrice?.let {
            db {
                where(table.price ge it)
            }
            redis {
                dependsOn(Book::price)
            }
        }

        maxPrice?.let {
            db {
                where(table.price le it)
            }
            redis {
                dependsOn(Book::price)
            }
        }

        db {
            orderBy(table.name)
        }
        redis {
            dependsOn(Book::name)
        }
    }
}