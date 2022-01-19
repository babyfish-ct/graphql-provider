package com.babyfish.graphql.provider.server.cfg

import org.babyfish.kimmer.Immutable
import java.math.BigDecimal

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

enum class BookSortedField {
    NAME,
    PRICE
}