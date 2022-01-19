package org.babyfish.graphql.provider.server.dsl

import org.babyfish.graphql.provider.server.dsl.redis.EntityPropRedisDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropRedisImpl
import org.babyfish.graphql.provider.server.runtime.DatabaseQuery
import org.babyfish.graphql.provider.server.runtime.Expression
import org.babyfish.graphql.provider.server.runtime.From
import org.babyfish.kimmer.Immutable
import java.util.function.Predicate
import kotlin.reflect.KProperty1

@GraphQLProviderDSL
class FilterDSL<E: Immutable> internal constructor(
    private val query: DatabaseQuery<E>,
    val redis: EntityPropRedisImpl
): EvalDSL(), DatabaseQuery<E> by query {

    fun redis(block: EntityPropRedisDSL<E>.() -> Unit) {
        EntityPropRedisDSL<E>(redis).block()
    }
}