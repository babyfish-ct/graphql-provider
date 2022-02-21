package com.babyfish.graphql.provider.model

import org.babyfish.kimmer.sql.Entity
import java.math.BigDecimal
import java.util.*

interface BookStore: Entity<UUID> {
    val name: String
    val books: List<Book>
    val avgPrice: BigDecimal
}