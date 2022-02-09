package com.babyfish.graphql.provider.starter.runtime.query

import com.babyfish.graphql.provider.starter.Book
import org.babyfish.graphql.provider.starter.runtime.query.*
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.expect

class SimpleExprTest: AbstractTest() {

    @Test
    fun testAndOr() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(
                    and(
                        table[Book::name] like "G",
                        null,
                        or(
                            table[Book::price] lt BigDecimal(20),
                            null,
                            table[Book::price] gt BigDecimal(30)
                        )
                    )
                )
            }
        )
        expect(""" from BOOK as table_1 
            |where (table_1.NAME like :1) 
            |and (table_1.PRICE < :2 or table_1.PRICE > :3)""".trimMarginToOneLine()
        ) { sql }
    }
}