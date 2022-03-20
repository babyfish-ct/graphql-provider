package org.babyfish.graphql.provider.example.model

import org.babyfish.kimmer.sql.Entity
import java.math.BigDecimal
import java.util.*

interface Book: Entity<UUID> {
    val name: String
    val store: BookStore?
    val edition: Int
    val price: Int
    val authors: List<Author>
}