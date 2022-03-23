package com.babyfish.graphql.provider.query

import com.babyfish.graphql.provider.model.Author
import com.babyfish.graphql.provider.model.name
import org.babyfish.graphql.provider.Query
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.ast.ilike

class AuthorQuery: Query() {

    suspend fun findAuthors(
        name: String?
    ): Connection<Author> =
        runtime.queryConnection {
            db {
                name?.let {
                    where(table.name ilike it)
                }
                orderBy(table.name)
            }
            redis {
                dependsOn(Author::name)
            }
        }
}