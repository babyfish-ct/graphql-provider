package com.babyfish.graphql.provider.mapper

import com.babyfish.graphql.provider.model.Book
import com.babyfish.graphql.provider.model.price
import com.babyfish.graphql.provider.model.store
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.ast.avg
import org.babyfish.kimmer.sql.ast.valueIn
import org.springframework.r2dbc.core.DatabaseClient
import java.math.BigDecimal
import java.util.*

class BookRepository(
    private val r2dbcClient: R2dbcClient
) {
    suspend fun findAvgPricesByStoreIds(
        storeIds: List<UUID>
    ): Map<UUID, BigDecimal> =
        r2dbcClient.execute(Book::class) {
            where(table.store.id valueIn storeIds)
            groupBy(table.store.id)
            select {
                table.store.id then table.price.avg().asNonNull()
            }
        }.associateBy({it.first}) {
            it.second
        }
}