package com.babyfish.graphql.provider.starter.cfg.query

import com.babyfish.graphql.provider.starter.cfg.Author
import org.babyfish.graphql.provider.starter.QueryService
import org.babyfish.graphql.provider.starter.runtime.expression.ilike
import org.babyfish.kimmer.graphql.Connection

class AuthorQueryService: QueryService() {

    suspend fun findAuthors(
        name: String?
    ): Connection<Author> = queryConnection {
        db {
            name?.let {
                where(table[Author::name] ilike  it)
            }
            orderBy(Author::name)
        }
        redis {
            dependsOn(Author::name)
        }
    }
}