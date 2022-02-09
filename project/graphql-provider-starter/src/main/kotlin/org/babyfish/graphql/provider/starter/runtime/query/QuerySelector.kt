package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable
import reactor.util.function.Tuple4
import kotlin.reflect.KProperty1

// Query

fun <T: Immutable, R> DatabaseQuery<T>.into(
    block: QuerySelector<T>.() -> TypedDatabaseQuery<T, R>
): TypedDatabaseQuery<T, R> =
    QuerySelector(this).block()

@JvmInline
value class QuerySelector<T: Immutable> internal constructor(
    val databaseQuery: DatabaseQuery<T>
) {
    inline val table: JoinableTable<T>
        get() = databaseQuery.table
}

fun <T: Immutable, R> QuerySelector<T>.select(
    prop: KProperty1<T, R?>
): TypedDatabaseQuery<T, R> =
    TypedQueryImpl(listOf(table[prop]), databaseQuery)

fun <T: Immutable, R> QuerySelector<T>.select(
    expr: Expression<R>
): TypedDatabaseQuery<T, R> =
    TypedQueryImpl(listOf(expr), databaseQuery)

fun <T: Immutable, A, B> QuerySelector<T>.select(
    expr1: Expression<A>,
    expr2: Expression<B>
): TypedDatabaseQuery<T, Pair<A, B>> =
    TypedQueryImpl(listOf(expr1, expr2), databaseQuery)

fun <T: Immutable, A, B, C> QuerySelector<T>.select(
    expr1: Expression<A>,
    expr2: Expression<B>,
    expr3: Expression<C>
): TypedDatabaseQuery<T, Triple<A, B, C>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3), databaseQuery)

fun <T: Immutable, T1, T2, T3, T4> QuerySelector<T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
): TypedDatabaseQuery<T, Tuple4<T1, T2, T3, T4>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3, expr4), databaseQuery)

fun <T: Immutable, T1, T2, T3, T4, T5> QuerySelector<T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
): TypedDatabaseQuery<T, Tuple5<T1, T2, T3, T4, T5>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5), databaseQuery)

fun <T: Immutable, T1, T2, T3, T4, T5, T6> QuerySelector<T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
): TypedDatabaseQuery<T, Tuple6<T1, T2, T3, T4, T5, T6>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6), databaseQuery)

fun <T: Immutable, T1, T2, T3, T4, T5, T6, T7> QuerySelector<T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
    expr7: Expression<T7>
): TypedDatabaseQuery<T, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6, expr7), databaseQuery)

fun <T: Immutable, T1, T2, T3, T4, T5, T6, T7, T8> QuerySelector<T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
    expr7: Expression<T7>,
    expr8: Expression<T8>,
): TypedDatabaseQuery<T, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6, expr7, expr8), databaseQuery)

fun <T: Immutable, T1, T2, T3, T4, T5, T6, T7, T8, T9> QuerySelector<T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
    expr7: Expression<T7>,
    expr8: Expression<T8>,
    expr9: Expression<T9>,
): TypedDatabaseQuery<T, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
    TypedQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6, expr7, expr8, expr9), databaseQuery)

// SubQuery

fun <P: Immutable, T: Immutable, R> DatabaseSubQuery<P, T>.into(
    block: SubQuerySelector<P, T>.() -> TypedDatabaseSubQuery<P, T, R>
): TypedDatabaseSubQuery<P, T, R> =
    SubQuerySelector(this).block()

@JvmInline
value class SubQuerySelector<P: Immutable, T: Immutable> internal constructor(
    val databaseSubQuery: DatabaseSubQuery<P, T>
) {
    inline val table: JoinableTable<T>
        get() = databaseSubQuery.table
}

fun <P: Immutable, T: Immutable, R> SubQuerySelector<P, T>.select(
    prop: KProperty1<T, R?>
): TypedDatabaseSubQuery<P, T, R> =
    TypedSubQueryImpl(listOf(table[prop]), databaseSubQuery)

fun <P: Immutable, T: Immutable, R> SubQuerySelector<P, T>.select(
    expr: Expression<R>
): TypedDatabaseSubQuery<P, T, R> =
    TypedSubQueryImpl(listOf(expr), databaseSubQuery)

fun <P: Immutable, T: Immutable, A, B> SubQuerySelector<P, T>.select(
    expr1: Expression<A>,
    expr2: Expression<B>
): TypedDatabaseSubQuery<P, T, Pair<A, B>> =
    TypedSubQueryImpl(listOf(expr1, expr2), databaseSubQuery)

fun <P: Immutable, T: Immutable, A, B, C> SubQuerySelector<P, T>.select(
    expr1: Expression<A>,
    expr2: Expression<B>,
    expr3: Expression<C>
): TypedDatabaseSubQuery<P, T, Triple<A, B, C>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3), databaseSubQuery)

fun <P: Immutable, T: Immutable, T1, T2, T3, T4> SubQuerySelector<P, T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
): TypedDatabaseSubQuery<P, T, Tuple4<T1, T2, T3, T4>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3, expr4), databaseSubQuery)

fun <P: Immutable, T: Immutable, T1, T2, T3, T4, T5> SubQuerySelector<P, T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
): TypedDatabaseSubQuery<P, T, Tuple5<T1, T2, T3, T4, T5>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5), databaseSubQuery)

fun <P: Immutable, T: Immutable, T1, T2, T3, T4, T5, T6> SubQuerySelector<P, T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
): TypedDatabaseSubQuery<P, T, Tuple6<T1, T2, T3, T4, T5, T6>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6), databaseSubQuery)

fun <P: Immutable, T: Immutable, T1, T2, T3, T4, T5, T6, T7> SubQuerySelector<P, T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
    expr7: Expression<T7>
): TypedDatabaseSubQuery<P, T, Tuple7<T1, T2, T3, T4, T5, T6, T7>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6, expr7), databaseSubQuery)

fun <P: Immutable, T: Immutable, T1, T2, T3, T4, T5, T6, T7, T8> SubQuerySelector<P, T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
    expr7: Expression<T7>,
    expr8: Expression<T8>,
): TypedDatabaseSubQuery<P, T, Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6, expr7, expr8), databaseSubQuery)

fun <P: Immutable, T: Immutable, T1, T2, T3, T4, T5, T6, T7, T8, T9> SubQuerySelector<P, T>.select(
    expr1: Expression<T1>,
    expr2: Expression<T2>,
    expr3: Expression<T3>,
    expr4: Expression<T4>,
    expr5: Expression<T5>,
    expr6: Expression<T6>,
    expr7: Expression<T7>,
    expr8: Expression<T8>,
    expr9: Expression<T9>,
): TypedDatabaseSubQuery<P, T, Tuple9<T1, T2, T3, T4, T5, T6, T7, T8, T9>> =
    TypedSubQueryImpl(listOf(expr1, expr2, expr3, expr4, expr5, expr6, expr7, expr8, expr9), databaseSubQuery)
