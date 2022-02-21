package com.babyfish.graphql.provider.mapper

import java.math.BigDecimal
import java.util.*

class BookRepository {

    suspend fun findAvgPrices(
        ids: List<UUID>
    ): Map<String, BigDecimal> {
        TODO()
    }
}