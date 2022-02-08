package org.babyfish.graphql.provider.example.model

import org.babyfish.kimmer.Immutable

interface Book: Immutable {
    val id: String
    val name: String
}