package com.babyfish.graphql.provider.query

import com.babyfish.graphql.provider.model.BookStore
import com.babyfish.graphql.provider.model.name
import org.babyfish.graphql.provider.Query
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.ast.ilike
import org.springframework.stereotype.Service

@Service
class BookStoreQuery : Query() {

    suspend fun findBookStores(
        name: String?
    ): Connection<BookStore> =
        runtime.queryConnection {
            db {
                name?.let {
                    where(table.name ilike it)
                }
                orderBy(BookStore::name)
            }
        }
}