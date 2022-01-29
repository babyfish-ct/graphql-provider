package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable

interface DatabaseSubQuery<P: Immutable, T: Immutable>: DatabaseQuery<T> {
    val parentTable: Table<P>
}

fun <P: Immutable, T: Immutable, R> DatabaseSubQuery<P, T>.select(
    block: DatabaseQuery<T>.() -> Expression<R>
): TypedDatabaseSubQuery<P, T, R> {
    TODO()
}

fun <P: Immutable, T: Immutable, A, B> DatabaseSubQuery<P, T>.select(
    block1: DatabaseQuery<T>.() -> Expression<A>,
    block2: DatabaseQuery<T>.() -> Expression<B>
): TypedDatabaseSubQuery<P, T, Pair<A, B>> {
    TODO()
}

fun <P: Immutable, T: Immutable, A, B, C> DatabaseSubQuery<P, T>.select(
    block1: DatabaseQuery<T>.() -> Expression<A>,
    block2: DatabaseQuery<T>.() -> Expression<A>,
    block3: DatabaseQuery<T>.() -> Expression<A>,
): TypedDatabaseSubQuery<P, T, Triple<A, B, C>> {
    TODO()
}

fun <P: Immutable, T: Immutable> DatabaseSubQuery<P, T>.selectAll(

): TypedDatabaseSubQuery<P, T, T> {
    TODO()
}