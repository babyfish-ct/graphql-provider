package org.babyfish.graphql.provider.starter.dsl

import io.r2dbc.spi.Statement
import org.babyfish.graphql.provider.starter.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.starter.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class UserImplementationDSL<E: Immutable, T> internal constructor(
    entityProp: EntityPropImpl
) {

    fun single(block: suspend ImplementationContext<E>.() -> T) {

    }

    fun batch(
        batchSize: Int? = null,
        block: suspend BatchImplementationContext<E>.() -> Map<out Any, T>
    ) {

    }

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