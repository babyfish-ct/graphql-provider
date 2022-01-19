package org.babyfish.graphql.provider.server.dsl

import io.r2dbc.spi.Statement
import org.babyfish.graphql.provider.server.dsl.redis.AbstractRedisDependencyDSL
import org.babyfish.graphql.provider.server.dsl.redis.EntityPropRedisDSL
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class ComputedDSL<E: Immutable, T> internal constructor(
    entityProp: EntityPropImpl
): ArgumentsDSL<E>(entityProp) {

    fun implementation(block: suspend ImplementationContext<E>.() -> T) {}

    fun batchImplementation(block: suspend BatchImplementationContext<E>.() -> Map<out Any, T>) {}

    fun redis(block: EntityPropRedisDSL<E>.() -> Unit) {

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