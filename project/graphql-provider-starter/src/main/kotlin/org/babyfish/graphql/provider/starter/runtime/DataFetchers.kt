package org.babyfish.graphql.provider.starter.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.starter.meta.QueryProp
import org.springframework.r2dbc.core.DatabaseClient

internal open class DataFetchers(
    val databaseClient: DatabaseClient
) {
    fun fetch(prop: QueryProp, env: DataFetchingEnvironment): Any {

        TODO()
    }
}