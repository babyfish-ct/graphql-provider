package com.babyfish.graphql.provider.server.cfg.query

import com.babyfish.graphql.provider.server.cfg.Book
import com.babyfish.graphql.provider.server.cfg.BookSortedField
import com.babyfish.graphql.provider.server.cfg.Query
import org.babyfish.graphql.provider.server.QueryAssembler
import org.babyfish.graphql.provider.server.dsl.QueryDSL
import org.babyfish.graphql.provider.server.runtime.ilike
import kotlin.reflect.KProperty1

class BookQueryAssembler: QueryAssembler<Query> {

    override fun QueryDSL<Query>.assemble() {

        connection(Query::findBooks) {

            optionalArgument("name", String::class) {
                where(table[Book::name] ilike it)
                redis {
                    dependsOn(Book::name)
                }
            }

            val sortedField = optionalArgument("sortedField", BookSortedField::class)
            val descending = optionalArgument("descending", Boolean::class)

            filter {
                val prop: KProperty1<Book,*> = when (sortedField.eval(BookSortedField.NAME)) {
                    BookSortedField.NAME -> Book::name
                    BookSortedField.PRICE -> Book::price
                }
                orderBy(prop, descending.eval(false))
                redis {
                    dependsOn(prop)
                }
            }
        }
    }
}