package com.babyfish.graphql.provider.server.cfg.assembler

import java.math.BigDecimal

class BookRepository {

    suspend fun findAvgPrices(
        ids: List<String>
    ): Map<String, BigDecimal> {
        TODO()
    }
}