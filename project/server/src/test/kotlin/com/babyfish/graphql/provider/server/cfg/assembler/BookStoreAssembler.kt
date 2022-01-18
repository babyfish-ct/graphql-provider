package com.babyfish.graphql.provider.server.cfg.assembler

import com.babyfish.graphql.provider.server.cfg.Author
import com.babyfish.graphql.provider.server.cfg.Book
import com.babyfish.graphql.provider.server.cfg.BookStore
import org.babyfish.graphql.provider.server.EntityAssembler
import org.babyfish.graphql.provider.server.cfg.EntityTypeConfiguration

class BookStoreAssembler: EntityAssembler<BookStore> {

    override fun EntityTypeConfiguration<BookStore>.assemble() {
        id(BookStore::id)
        mappedList(BookStore::books, Book::store) {
            redis {
                dependsOnList(Book::authors) {
                    dependsOn(Author::name)
                }
            }
        }
    }
}

data class Node(
    val name: String,
    val childNodes: List<Node>
)
