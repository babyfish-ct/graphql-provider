package org.babyfish.graphql.provider.example.model.security

import org.babyfish.graphql.provider.example.model.security.AppUser
import org.babyfish.kimmer.sql.Entity
import java.util.*

interface Role: Entity<UUID> {
    val name: String
    val appUsers: List<AppUser>
}