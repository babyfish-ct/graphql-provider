package org.babyfish.graphql.provider.example.model

import org.babyfish.kimmer.sql.Entity
import java.util.*

interface BookStore: Entity<UUID> {
    val name: String
    val website: String?
    val books: List<Book>
    val avgPrice: Int
}