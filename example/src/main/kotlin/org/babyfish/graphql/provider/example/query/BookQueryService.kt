package org.babyfish.graphql.provider.example.query

import org.babyfish.graphql.provider.starter.QueryService
import org.springframework.stereotype.Component

@Component
class BookQueryService: QueryService() {

    fun findBooks(): String = "hello"
}