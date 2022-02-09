package com.babyfish.graphql.provider.starter.runtime.query

import com.babyfish.graphql.provider.starter.Author
import com.babyfish.graphql.provider.starter.Book
import org.babyfish.graphql.provider.starter.runtime.query.*
import org.junit.Test
import kotlin.test.expect

class SubQueryTest: AbstractTest() {

    @Test
    fun testIn() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(
                    table[Book::id] valueIn typedSubQuery(Author::class) {

                        where(table[Author::name] eq "Alex")

                        into {
                            select(table.joinList(Author::books)[Book::id])
                        }
                    }
                )
            }
        )
        expect(""" from BOOK as table_1 
            |where table_1.ID in (
                |select table_3.BOOK_ID from AUTHOR as table_2 
                |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.AUTHOR_ID 
                |where table_2.NAME = :1
            |)""".trimMarginToOneLine()
        ) { sql }
        expect(listOf("Alex")) { variables }
    }

    @Test
    fun testExists() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(
                    exists(subQuery(Author::class) {
                        where(
                            table.joinList(Author::books)[Book::id] eq parentTable[Book::id],
                            table[Author::name] eq "Alex"
                        )
                    })
                )
            }
        )
        expect(""" from BOOK as table_1 
            |where exists(
                |select * from AUTHOR as table_2 
                |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.AUTHOR_ID 
                |where table_3.BOOK_ID = table_1.ID 
                |and table_2.NAME = :1
            |)""".trimMarginToOneLine()) { sql }
        expect(listOf("Alex")) { variables }
    }

    @Test
    fun testNotIn() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(
                    table[Book::id] valueNotIn typedSubQuery(Author::class) {

                        where(table[Author::name] eq "Alex")

                        into {
                            select(table.joinList(Author::books)[Book::id])
                        }
                    }
                )
            }
        )
        expect(""" from BOOK as table_1 
            |where table_1.ID not in (
                |select table_3.BOOK_ID from AUTHOR as table_2 
                |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.AUTHOR_ID 
                |where table_2.NAME = :1
            |)""".trimMarginToOneLine()
        ) { sql }
        expect(listOf("Alex")) { variables }
    }

    @Test
    fun testNotExists() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(
                    notExists(subQuery(Author::class) {
                        where(
                            table.joinList(Author::books)[Book::id] eq parentTable[Book::id],
                            table[Author::name] eq "Alex"
                        )
                    })
                )
            }
        )
        expect(""" from BOOK as table_1 
            |where not exists(
                |select * from AUTHOR as table_2 
                |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.AUTHOR_ID 
                |where table_3.BOOK_ID = table_1.ID 
                |and table_2.NAME = :1
            |)""".trimMarginToOneLine()) { sql }
        expect(listOf("Alex")) { variables }
    }
}