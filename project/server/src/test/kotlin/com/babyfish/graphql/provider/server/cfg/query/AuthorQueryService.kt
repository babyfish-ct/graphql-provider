package com.babyfish.graphql.provider.server.cfg.query

import com.babyfish.graphql.provider.server.cfg.Author
import org.babyfish.graphql.provider.server.QueryService
import org.babyfish.graphql.provider.server.runtime.ilike
import org.babyfish.kimmer.Connection

class AuthorQueryService: QueryService() {

    suspend fun findAuthors(
        name: String?
    ): Connection<Author> = queryConnection {
        db {
            name?.let {
                where(table[Author::name] ilike it)
            }
            orderBy(Author::name)
        }
        redis {
            dependsOn(Author::name)
        }
    }
}