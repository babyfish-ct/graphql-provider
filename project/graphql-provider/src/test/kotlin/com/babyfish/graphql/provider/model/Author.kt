package com.babyfish.graphql.provider.model

import org.babyfish.kimmer.sql.Entity
import java.util.*

interface Author: Entity<UUID> {
    val name: String
    val books: List<Book>
}