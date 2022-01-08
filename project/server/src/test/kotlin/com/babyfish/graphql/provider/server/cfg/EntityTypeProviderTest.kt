package com.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.EntityTypeProvider
import org.babyfish.graphql.provider.server.cfg.EntityConfiguration
import org.babyfish.graphql.provider.server.cfg.length
import org.babyfish.graphql.provider.server.cfg.precision
import org.babyfish.graphql.provider.server.cfg.scale
import org.babyfish.graphql.provider.server.meta.Arg
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

@Component
class BookStoreAssembler: EntityAssembler<BookStore>(
    private val computeServer: ComputeServeice
) {

    override fun EntityConfiguration<BookStore>.assemble() {
        id(BookStore::id)
        mappedList(BookStore::books, Book::store) {
            filter(Arg("authorName", String::class)) {
                args.string("authorName")?.let {
                    query.addCondition(
                        query.like(query.join(Book::authors).get(Author::name), it)
                    )
                }
            }
            redis {
                dependsOnList(Book::authors) {
                    dependsOn(Author::name)
                }
            }
        }
        computed(BookStore::avgPrice) {
            batchImplementation {
                computedServer.blabla()
            }
            redis {
                dependsOn(BookStore::books) {
                    dependsOn(Book::price)
                }
            }
        }
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

        scalar(Book::price) {
            db {
                precision(10)
                scale(2)
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