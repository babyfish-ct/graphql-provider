package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface DatabaseQuery<T: Immutable> {

    val table: JoinableTable<T>

    fun where(vararg predicates: Expression<Boolean>?)

    fun orderBy(expression: Expression<*>?, descending: Boolean = false)

    fun orderBy(prop: KProperty1<T, *>, descending: Boolean = false) {
        orderBy(table[prop], descending)
    }

    fun <X: Immutable> subQuery(
        type: KClass<X>,
        block: (DatabaseSubQuery<T, X>.() -> Unit)? = null
    ): DatabaseSubQuery<T, X>
}

fun <T: Immutable, R> DatabaseQuery<T>.select(
    prop: KProperty1<T, R>
): TypedDatabaseQuery<T, R> =
    select {
        table[prop]
    }

fun <T: Immutable, R> DatabaseQuery<T>.select(
    block: DatabaseQuery<T>.() -> Expression<R>
): TypedDatabaseQuery<T, R> {
    TODO()
}

fun <T: Immutable, A, B> DatabaseQuery<T>.select(
    block1: DatabaseQuery<T>.() -> Expression<A>,
    block2: DatabaseQuery<T>.() -> Expression<B>
): TypedDatabaseQuery<T, Pair<A, B>> {
    TODO()
}

fun <T: Immutable, A, B, C> DatabaseQuery<T>.select(
    block1: DatabaseQuery<T>.() -> Expression<A>,
    block2: DatabaseQuery<T>.() -> Expression<B>,
    block3: DatabaseQuery<T>.() -> Expression<C>,
): TypedDatabaseQuery<T, Triple<A, B, C>> {
    TODO()
}

fun <T: Immutable> DatabaseQuery<T>.selectAll(

): TypedDatabaseQuery<T, T> {
    TODO()
}

