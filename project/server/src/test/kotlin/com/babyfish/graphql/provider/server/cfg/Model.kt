package com.babyfish.graphql.provider.server.cfg

import io.r2dbc.spi.Readable
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.babyfish.graphql.provider.server.meta.Arg
import org.babyfish.graphql.provider.server.meta.OnDeleteAction
import org.babyfish.kimmer.Immutable
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
