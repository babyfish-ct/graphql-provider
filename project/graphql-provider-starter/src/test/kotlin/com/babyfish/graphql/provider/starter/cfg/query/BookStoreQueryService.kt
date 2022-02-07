package com.babyfish.graphql.provider.starter.cfg.query

import com.babyfish.graphql.provider.starter.cfg.BookStore
import org.babyfish.graphql.provider.starter.QueryService
import org.babyfish.graphql.provider.starter.runtime.expression.ilike
import org.babyfish.kimmer.graphql.Connection
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