package com.babyfish.graphql.provider.query

import com.babyfish.graphql.provider.model.Book
import com.babyfish.graphql.provider.model.name
import com.babyfish.graphql.provider.model.price
import org.babyfish.graphql.provider.Query
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.ast.ge
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.le
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.reflect.KProperty1

@Component
class BookQuery: Query() {

    fun findBooks(
        name: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        descending: Boolean = false
    ): Connection<Book> =
        runtime.queryConnection {

            name?.let {
                db {
                    where(table.name ilike it)
                }
            }

            minPrice?.let {
                db {
                    where(table.price ge it)
                }
            }

            maxPrice?.let {
                db {
                    where(table.price le it)
                }
            }

            db {
                orderBy(table.name)
            }
        }
}
