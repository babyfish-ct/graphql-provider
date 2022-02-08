package com.babyfish.graphql.provider.starter.mapper

import java.math.BigDecimal

class BookRepository {

    suspend fun findAvgPrices(
        ids: List<String>
    ): Map<String, BigDecimal> {
        TODO()
    }
}