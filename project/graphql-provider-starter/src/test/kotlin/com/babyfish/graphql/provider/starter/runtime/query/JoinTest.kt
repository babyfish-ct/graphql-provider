package com.babyfish.graphql.provider.starter.runtime.query

import com.babyfish.graphql.provider.starter.Author
import com.babyfish.graphql.provider.starter.Book
import com.babyfish.graphql.provider.starter.BookStore
import org.babyfish.graphql.provider.starter.runtime.query.*
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.expect

class JoinTest : AbstractTest() {

    @Test
    fun testSingle() {
        val (sql, variables) = render(query(Book::class))
        expect(" from BOOK as table_1") { sql }
        expect(emptyList()) { variables }
    }

    @Test
    fun testMergedJoinFromParentToChild() {
        val (sql, variables) = render(
            query(BookStore::class).apply {
                where(table.joinList(BookStore::books, JoinType.LEFT)[Book::price] ge BigDecimal(20))
                where(table.joinList(BookStore::books, JoinType.RIGHT)[Book::price] le BigDecimal(30))
                where(
                    table.joinList(BookStore::books).joinList(Book::authors)[Author::name] ilike "Alex"
                )
            }
        )
        expect(""" from BOOK_STORE as table_1 
            |inner join BOOK as table_2 on table_1.ID = table_2.STORE_ID 
            |inner join BOOK_AUTHOR_MAPPING as table_3 on table_2.ID = table_3.BOOK_ID 
            |inner join AUTHOR as table_4 on table_3.AUTHOR_ID = table_4.ID 
            |where table_2.PRICE >= :1 
            |and table_2.PRICE <= :2 
            |and lower(table_4.NAME) like :3""".trimMarginToOneLine()
        ) { sql }
        expect(listOf(BigDecimal(20), BigDecimal(30), "alex")) { variables }
    }

    @Test
    fun testMergedJoinFromChildToParent() {
        val (sql, variables) = render(
            query(Author::class).apply {
                where(table.joinList(Author::books, JoinType.LEFT)[Book::price] le BigDecimal(20))
                where(table.joinList(Author::books, JoinType.LEFT)[Book::price] le BigDecimal(30))
                where(
                    table.joinList(Author::books).joinReference(Book::store)[BookStore::name] ilike "MANNING"
                )
            }
        )
        expect(""" from AUTHOR as table_1 
            |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.AUTHOR_ID 
            |inner join BOOK as table_3 on table_2.BOOK_ID = table_3.ID 
            |inner join BOOK_STORE as table_4 on table_3.STORE_ID = table_4.ID 
            |where table_3.PRICE <= :1 
            |and table_3.PRICE <= :2 
            |and lower(table_4.NAME) like :3""".trimMarginToOneLine()
        ) { sql }
        expect(listOf(BigDecimal(20), BigDecimal(30), "manning")) { variables }
    }

    @Test
    fun testUnnecessaryJoin() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(table.joinReference(Book::store)[BookStore::id] valueIn listOf("id1", "id2"))
            }
        )
        expect(" from BOOK as table_1 where table_1.STORE_ID in (:1, :2)") {sql}
        expect(listOf("id1", "id2")) { variables }
    }

    @Test
    fun testHalfJoin() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(table.joinList(Book::authors)[Author::id] valueIn listOf("id1", "id2"))
            }
        )
        expect(""" from BOOK as table_1 
            |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.BOOK_ID 
            |where table_2.AUTHOR_ID in (:1, :2)""".trimMarginToOneLine()
        ) { sql }
        expect(listOf("id1", "id2")) { variables }
    }

    @Test
    fun testHalfInverseJoin() {
        val (sql, variables) = render(
            query(Author::class).apply {
                where(table.joinList(Author::books)[Book::id] valueIn listOf("id1", "id2"))
            }
        )
        expect(""" from AUTHOR as table_1 
            |inner join BOOK_AUTHOR_MAPPING as table_2 on table_1.ID = table_2.AUTHOR_ID 
            |where table_2.BOOK_ID in (:1, :2)""".trimMarginToOneLine()
        ) { sql }
        expect(listOf("id1", "id2")) { variables }
    }

    @Test
    fun testOneToManyCannotBeOptimized() {
        val (sql, variables) = render(
            query(BookStore::class).apply {
                where(table.joinList(BookStore::books)[Book::id] valueIn listOf("id1", "id2"))
            }
        )
        expect(""" from BOOK_STORE as table_1 
            |inner join BOOK as table_2 on table_1.ID = table_2.STORE_ID 
            |where table_2.ID in (:1, :2)""".trimMarginToOneLine()
        ) { sql }
        expect(listOf("id1", "id2")) { variables }
    }

    @Test
    fun testOuterJoin() {
        val (sql, variables) = render(
            query(Book::class).apply {
                where(
                    or(
                        table.joinReference(Book::store, JoinType.LEFT)[BookStore::id].isNull(),
                        table.joinReference(Book::store, JoinType.LEFT)[BookStore::name] ilike "MANNING"
                    )
                )
            }
        )
        expect(""" from BOOK as table_1 
            |left join BOOK_STORE as table_2 on table_1.STORE_ID = table_2.ID 
            |where table_1.STORE_ID is null 
            |or lower(table_2.NAME) like :1""".trimMarginToOneLine()
        ) { sql }
        expect(listOf("manning")) { variables }
    }
}