package org.babyfish.graphql.provider.dsl

import io.r2dbc.spi.Statement
import org.babyfish.graphql.provider.dsl.redis.FilterRedisDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.kimmer.Immutable

@GraphQLProviderDSL
class UserImplementationDSL<E: Immutable, T> internal constructor(
    private val entityProp: ModelPropImpl
) {
    private var single: (suspend ImplementationContext<*>.() -> Any?)? = null

    private var batch: (suspend ImplementationContext<*>.() -> Map<out Any, *>)? = null

    private var batchSize: Int = 10

    @Suppress("UNCHECKED_CAST")
    fun single(block: suspend ImplementationContext<E>.() -> T) {
        entityProp.userImplementation?.apply {
            single = block as (suspend ImplementationContext<*>.() -> Any?)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun batch(
        batchSize: Int? = null,
        block: suspend BatchImplementationContext<E>.() -> Map<out Any, T>
    ) {
        entityProp.userImplementation?.apply {
            this.batchSize = batchSize
            batch = block as (suspend BatchImplementationContext<*>.() -> Map<out Any, *>)
        }
    }

//    fun redis(block: FilterRedisDSL.() -> Unit) {
//
//    }
}

interface ImplementationContext<E> {
    val row: E
    fun createStatement(sql: String): Statement
}

interface BatchImplementationContext<E> {
    val rows: List<E>
    fun createStatement(sql: String): Statement
}