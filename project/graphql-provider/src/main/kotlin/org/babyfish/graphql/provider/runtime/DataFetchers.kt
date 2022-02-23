package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.QueryProp
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass

internal open class DataFetchers(
    private val r2dbcClient: R2dbcClient
) {
    @Suppress("UNCHECKED_CAST")
    suspend fun fetch(prop: QueryProp, env: DataFetchingEnvironment): Any? =
        r2dbcClient.execute(
            prop.targetType!!.kotlinType as KClass<Entity<String>>,
        ) {
            prop.filter.execute(env, FilterExecutionContext(this, mutableSetOf()))
            select(table)
        }
}