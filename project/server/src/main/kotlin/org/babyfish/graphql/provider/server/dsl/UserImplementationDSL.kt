package org.babyfish.graphql.provider.server.dsl

import io.r2dbc.spi.Statement
import org.babyfish.graphql.provider.server.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.server.dsl.redis.RedisDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class UserImplementationDSL<E: Immutable, T> internal constructor(
    entityProp: EntityPropImpl
) {

    fun single(block: suspend ImplementationContext<E>.() -> T) {}

    fun batch(block: suspend BatchImplementationContext<E>.() -> Map<out Any, T>) {}

    fun redis(block: FilterRedisDSL.() -> Unit) {

    }
}

interface ImplementationContext<E> {
    val row: E
    fun createStatement(sql: String): Statement
}

interface BatchImplementationContext<E> {
    val rows: List<E>
    fun createStatement(sql: String): Statement
}