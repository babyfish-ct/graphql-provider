package org.babyfish.graphql.provider.example.query

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.Query
import org.springframework.stereotype.Component

@Component
class BookQuery: Query() {

    fun findBooks(name: String?): List<Book> =
        queryList {

        }
}