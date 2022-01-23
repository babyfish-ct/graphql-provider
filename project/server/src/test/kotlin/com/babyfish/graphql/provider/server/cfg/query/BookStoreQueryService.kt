package com.babyfish.graphql.provider.server.cfg.query

import com.babyfish.graphql.provider.server.cfg.BookStore
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.runtime.ilike
import org.babyfish.kimmer.Connection
import org.springframework.stereotype.Component

@Component
class BookStoreQueryService : QueryService() {

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