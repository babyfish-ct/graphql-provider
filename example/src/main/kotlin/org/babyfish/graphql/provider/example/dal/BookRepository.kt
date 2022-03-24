package org.babyfish.graphql.provider.example.dal

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.graphql.provider.example.model.price
import org.babyfish.graphql.provider.example.model.store
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.ast.avg
import org.babyfish.kimmer.sql.ast.valueIn
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

@Repository
class BookRepository(
    private val r2dbcClient: R2dbcClient
) {
    suspend fun findAvgPriceGroupByStoreIds(
        storeIds: Collection<UUID>
    ): Map<UUID, BigDecimal> =
        r2dbcClient.query(Book::class) {
            select {
                where(table.store.id valueIn storeIds)
                groupBy(table.store.id)
                table.store.id then
                    table.price.avg().asNonNull()
            }
        }.associateBy({it.first}) {
            it.second
        }
}