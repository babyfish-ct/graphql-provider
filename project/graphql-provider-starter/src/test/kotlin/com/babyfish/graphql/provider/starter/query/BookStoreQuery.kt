package com.babyfish.graphql.provider.starter.query

import com.babyfish.graphql.provider.starter.BookStore
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.runtime.query.ilike
import org.babyfish.kimmer.graphql.Connection
import org.springframework.stereotype.Component

@Component
class BookStoreQuery : Query() {

    suspend fun findBookStores(
        name: String?
    ): Connection<BookStore> = queryConnection {

        db {
            name?.let {
                where(table[BookStore::name] ilike it)
            }
            orderBy(BookStore::name)
        }

        redis {
            dependsOn(BookStore::name)
        }
    }
}