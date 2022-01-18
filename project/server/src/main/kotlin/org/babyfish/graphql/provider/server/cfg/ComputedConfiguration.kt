package org.babyfish.graphql.provider.server.cfg

import io.r2dbc.spi.Statement
import org.babyfish.graphql.provider.server.meta.impl.EntityPropImpl

class ComputedConfiguration<E, T> internal constructor(
    private val entityProp: EntityPropImpl
) {

    fun implementation(block: suspend ImplementationContext<E>.() -> T) {}

    fun batchImplementation(block: suspend BatchImplementationContext<E>.() -> List<Pair<Any, T>>) {}

    fun redis(block: RedisConfiguration<E>.() -> Unit) {

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