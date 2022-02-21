package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.QueryType

internal class QueryTypeImpl : QueryType {

    override val name: String
        get() = "Query"

    override val props = mutableMapOf<String, QueryPropImpl>()
}