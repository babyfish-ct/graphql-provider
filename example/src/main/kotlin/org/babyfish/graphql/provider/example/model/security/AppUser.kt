package org.babyfish.graphql.provider.example.model.security

import org.babyfish.graphql.provider.example.model.Book
import org.babyfish.kimmer.sql.Entity
import java.util.*

interface AppUser: Entity<UUID> {

    val email: String
    val password: String

    val roles: List<Role>

    // More fields for real project, such as:
    // name, gender, birthday, photo, twitter, facebook, website, etc
}