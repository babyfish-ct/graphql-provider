package org.babyfish.graphql.provider.example.model

import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.kimmer.sql.Entity
import java.math.BigDecimal
import java.util.*

interface Book: Entity<UUID> {
    val name: String
    val store: BookStore?
    val price: BigDecimal
    val authors: List<Author>
    val fans: List<AppUser>
}