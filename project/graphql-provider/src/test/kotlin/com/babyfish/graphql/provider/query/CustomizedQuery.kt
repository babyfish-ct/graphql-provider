package com.babyfish.graphql.provider.query

import org.babyfish.graphql.provider.Query
import org.springframework.stereotype.Service

@Service
class CustomizedQuery: Query() {

    fun login(userName: String, password: String): String =
        runtime.query {
            ""
        }
}