package com.babyfish.graphql.provider.server.cfg.model

import java.math.BigDecimal

class BookRepository {

    suspend fun findAvgPrices(
        ids: List<String>
    ): Map<String, BigDecimal> {
        TODO()
    }
}