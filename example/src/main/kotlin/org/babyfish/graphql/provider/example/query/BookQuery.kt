package org.babyfish.graphql.provider.example.query

import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.example.model.*
import org.babyfish.kimmer.sql.ast.ilike
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.ast.valueNotIn
import org.springframework.stereotype.Component
import java.util.*

@Component
class BookQuery: Query() {

    fun findBooks(
        name: String?,
        inclusiveStoreIds: List<UUID>?,
        exclusiveStoreIds: List<UUID>?,
        inclusiveAuthorIds: List<UUID>?,
        exclusiveAuthorIds: List<UUID>?,
    ): List<Book> =
        queryList {
            name?.let {
                db {
                    where(table.name ilike it)
                }
            }
            inclusiveStoreIds?.let {
                db {
                    where(table.store.id valueIn it)
                }
            }
            exclusiveStoreIds?.let {
                db {
                    where(table.store.id valueNotIn it)
                }
            }
            inclusiveAuthorIds?.let {
                db {
                    where {
                        table.id valueIn subQuery(Author::class) {
                            where(table.id valueIn it)
                            select(table.books.id)
                        }
                    }
                }
            }
            exclusiveAuthorIds?.let {
                db {
                    where {
                        table.id valueNotIn subQuery(Author::class) {
                            where(table.id valueIn it)
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