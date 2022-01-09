package com.babyfish.graphql.provider.server.cfg

import io.r2dbc.spi.Readable
import io.r2dbc.spi.Result
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.graphql.provider.kimmer.*
import org.babyfish.graphql.provider.server.meta.Arg
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
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
                dependsOn(Book::name)
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
                dependsOn(Author::name)
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
                dependsOn(Book::name)
            }
        }
    }
}