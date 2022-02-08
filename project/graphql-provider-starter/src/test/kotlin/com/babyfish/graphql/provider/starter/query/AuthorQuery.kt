package com.babyfish.graphql.provider.starter.query

import com.babyfish.graphql.provider.starter.Author
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.runtime.query.ilike
import org.babyfish.kimmer.graphql.Connection

class AuthorQuery: Query() {

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