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
            db {
                name?.let {
                    where(table.name ilike it)
                }
                storeName?.let {
                    where(table.store.name ilike it)
                }
                if (authorFirstName !== null || authorLastName !== null) {
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
                orderBy(table.name)
            }
        }
}

/*
 * When authorFirstName is not null or authorLastName is not null,
 * a sub-query is used to filter data.
 *
 * Since kimmer-sql-0.3.3, the middle table hidden by entity model
 * can be select and modified directly, so you can also write the
 * sub-query like this
 *
 * where {
 *     table.id valueIn subQueries.byList(Book::authors) {
 *         authorFirstName?.let {
 *             where(table.target.firstName ilike it)
 *         }
 *         authorLastName?.let {
 *             where(table.target.lastName ilike it)
 *         }
 *         select(table.source.id)
 *     }
 * }
 *
 * All of these two methods work fine,
 * with the same functionality and same performance.
 * (
 *     "select(table.books.id)" in the example code can be optimized by half-join,
 *     "select(table.source.id)" here can be optimized by phantom-join.
 *     So, both of them are high-performance practices.
 *
 *     Please read
 *     "https://github.com/babyfish-ct/kimmer/blob/main/doc/kimmer-sql/table-joins.md"
 *     to learn more
 * ).
 *
 * You're free to choose whichever method you prefer.
 */