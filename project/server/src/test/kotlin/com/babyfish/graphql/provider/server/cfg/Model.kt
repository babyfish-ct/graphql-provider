package com.babyfish.graphql.provider.server.cfg

import io.r2dbc.spi.Readable
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.graphql.provider.kimmer.*
import org.babyfish.graphql.provider.server.cfg.Arg
import org.babyfish.graphql.provider.server.cfg.OnDeleteAction
import org.babyfish.graphql.provider.server.cfg.entity
import java.math.BigDecimal
import java.util.function.Function

interface Node: Immutable {
    val id: String
}

interface BookStore: Node {
    val name: String
    val books: List<Book>
    val avgPrice: BigDecimal
}

interface Book: Node {
    val name: String
    val price: BigDecimal
    val store: BookStore?
    val authors: List<Author>
}

interface Author: Node {
    val name: String
    val books: List<Book>
}

interface NodeDraft<T: Node>: Node, Draft<T> {
    override var id: String
    interface Sync: NodeDraft<Node>, SyncDraft<Node>
    interface Async: NodeDraft<Node>, AsyncDraft<Node>
}

interface BookStoreDraft<T: BookStore>: BookStore, NodeDraft<T> {
    override var books: MutableList<BookDraft<out Book>>
    override var avgPrice: BigDecimal
    interface Sync: BookStoreDraft<BookStore>, SyncDraft<BookStore>
    interface Async: BookStoreDraft<BookStore>, AsyncDraft<BookStore>
}

interface BookDraft<T: Book>: Book, NodeDraft<T> {
    override var name: String
    override var price: BigDecimal
    override var store: BookStore?
    override var authors: MutableList<AuthorDraft<out Author>>
    interface Sync: BookDraft<Book>, SyncDraft<Book>
    interface Async: BookDraft<Book>, AsyncDraft<Book>
}

interface AuthorDraft<T: Author>: Author, NodeDraft<T> {
    override var name: String
    override var books: MutableList<BookDraft<out Book>>
    interface Sync: AuthorDraft<Author>, SyncDraft<Author>
    interface Async: AuthorDraft<Author>, AsyncDraft<Author>
}

val book = new(BookDraft.Sync::class) {
    id = "00001"
    name = "Learning GraphQL"
    authors += new(AuthorDraft.Sync::class) {
        id = "00002"
        name = "Jim"
    }
    authors += new(AuthorDraft.Sync::class) {
        id = "00003"
        name = "Kate"
    }
}

val book2 = new(BookDraft.Sync::class, book) {
    name += "!"
    for (author in authors) {
        author.name += "!"
    }
}

fun init() {

    entity(BookStore::class) {
        db {
            table("BOOK_STORE")
        }
        mappedList(BookStore::books, Book::store) {
            filter(Arg("name", String::class)) {
                args.string("name")?.let {
                    query.apply {
                        addCondition(
                            like(table[Book::name], it)
                        )
                    }
                }
            }
            redis {
                dependsOnList(BookStore::books) {
                    dependsOn(Book::name)
                }
            }
        }
        computed(BookStore::avgPrice) {
            batchImplementation {
                createStatement(
                    "select book_store_id, avg(price) from where book_store_id in (${
                        List(rows.size) { i -> "?${i + 1}" }.joinToString()
                    }) book group by book_store_id"
                )
                    .apply {
                        for (i in rows.indices) {
                            bind("${i + 1}", rows[i].id)
                        }
                    }
                    .execute()
                    .awaitSingle()
                    .map(Function<Readable, Pair<String, BigDecimal>> {
                        it.get(0, String::class.java) to it.get(1, BigDecimal::class.java)
                    })
                    .asFlow()
                    .toList()
            }
            redis {
                dependsOnList(BookStore::books) {
                    dependsOn(Book::price)
                }
            }
        }
    }

    entity(Book::class) {
        db {
            table("BOOK")
        }
        reference(Book::store) {
            db {
                foreignKey("BOOK_ID", onDelete = OnDeleteAction.CASCADE)
            }
        }
        list(Book::authors) {
            db {
                middleTable("BOOK_AUTHOR_MAPPING", "BOOK_ID", "AUTHOR_ID")
            }
            filter(Arg("name", String::class)) {
                args.string("name")?.let {
                    query.apply {
                        addCondition(
                            like(table[Author::name], it)
                        )
                    }
                }
            }
            redis {
                dependsOnList(Book::authors) {
                    dependsOn(Book::name)
                }
            }
        }
    }

    entity(Author::class) {
        db {
            table("AUTHOR")
        }
        mappedList(Author::books, Book::authors) {
            filter(Arg("name", String::class)) {
                args.string("name")?.let {
                    query.apply {
                        addCondition(
                            like(table[Book::name], it)
                        )
                    }
                }
            }
            redis {
                dependsOnList(Author::books) {
                    dependsOn(Book::name)
                }
            }
        }
    }
}