package org.babyfish.graphql.prodiver.kimmer.meta

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import kotlin.test.Test

interface Node: Immutable {
    val id: String
}

interface BookStore: Node {
    val name: String
    val books: List<Book>
}

interface Book: Node {
    val name: String
    val store: BookStore?
    val authors: List<Author>
}

interface Author: Node {
    val name: String
    val books: List<Book>
}

class MetaTest {

    @Test
    fun test() {
        ImmutableType.of(Book::class)
    }
}