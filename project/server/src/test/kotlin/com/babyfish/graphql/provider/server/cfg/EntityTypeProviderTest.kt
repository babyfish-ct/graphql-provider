package com.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.EntityTypeProvider
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration
import kotlin.test.Test

class EntityTypeProviderTest {

    @Test
    fun test() {
        val entityTypeProvider = EntityTypeProvider(
            listOf(
                BookStoreAssembler(),
                BookAssembler(),
                AuthorAssembler()
            )
        )
        println(entityTypeProvider[BookStore::class])
        println(entityTypeProvider[Book::class])
        println(entityTypeProvider[Author::class])
    }
}

class BookStoreAssembler: EntityAssembler<BookStore> {

    override fun EntityConfiguration<BookStore>.assemble() {
        id(BookStore::id)
        mappedList(BookStore::books, Book::store)
    }
}

class BookAssembler: EntityAssembler<Book> {

    override fun EntityConfiguration<Book>.assemble() {
        id(Book::id)

        reference(Book::store) {
            db {
                foreignKey("STORE_ID")
            }
        }
        list(Book::authors) {
            db {
                middleTable(
                    "BOOK_AUTHOR_MAPPING",
                    "BOOK_ID",
                    "AUTHOR_ID"
                )
            }
        }
    }
}

class AuthorAssembler: EntityAssembler<Author> {

    override fun EntityConfiguration<Author>.assemble() {
        id(Author::id)
        mappedList(Author::books, Book::authors)
    }
}