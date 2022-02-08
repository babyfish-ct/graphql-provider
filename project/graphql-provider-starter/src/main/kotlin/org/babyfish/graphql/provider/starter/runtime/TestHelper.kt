package org.babyfish.graphql.provider.starter.runtime

import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.runtime.query.DatabaseQuery
import org.babyfish.graphql.provider.starter.runtime.query.QueryImpl
import org.babyfish.graphql.provider.starter.runtime.query.Renderable
import org.babyfish.graphql.provider.starter.runtime.query.SqlBuilder
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass

internal class TestHelper {

    companion object {

        @JvmStatic
        fun <T: Immutable> query(
            queries: List<Query>,
            mappers: List<EntityMapper<*>>,
            type: KClass<T>
        ): DatabaseQuery<T> =
            GraphQLTypeGenerator(queries, mappers).let {
                it.generate()
                it.entityTypes
            }.let {
                QueryImpl<T>(it, type)
            }

        @JvmStatic
        fun render(query: DatabaseQuery<*>): Pair<String, List<Any?>> =
            SqlBuilder().let {
                (query as Renderable).renderTo(it)
                it.builder.toString() to it.variables
            }
    }
}