package org.babyfish.graphql.provider.example.query

import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.example.model.*
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.valueIn
import org.springframework.stereotype.Service

@Service
class BookQuery: Query() {

    suspend fun books(
        name: String?,
        storeName: String?,
        authorFirstName: String?,
        authorLastName: String?
    ): Connection<Book> =
        runtime.queryConnection {
            name?.let {
                db {
                    where(table.name ilike it)
                }
            }
            storeName?.let {
                db {
                    where(table.store.name ilike it)
                }
            }
            if (authorFirstName !== null || authorLastName !== null) {
                db {
                    where {
                        table.id valueIn subQuery(Author::class) {
                            authorFirstName?.let {
                                where(table.firstName ilike it)
                            }
                            authorLastName?.let {
                                where(table.lastName ilike it)
                            }
                            select(table.books.id)
                        }
                    }
                }
            }
            db {
                orderBy(table.name)
            }
        }
}