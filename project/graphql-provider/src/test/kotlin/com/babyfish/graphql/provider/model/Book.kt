package com.babyfish.graphql.provider.model

import org.babyfish.kimmer.sql.Entity
import java.math.BigDecimal
import java.util.*

interface Book: Entity<UUID> {
    val name: String
    val price: BigDecimal
    val store: BookStore?
    val authors: List<Author>
}