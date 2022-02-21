package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.QueryProp

internal open class DataFetchers(
    //val databaseClient: DatabaseClient
) {
    fun fetch(prop: QueryProp, env: DataFetchingEnvironment): Any {

        TODO()
    }
}