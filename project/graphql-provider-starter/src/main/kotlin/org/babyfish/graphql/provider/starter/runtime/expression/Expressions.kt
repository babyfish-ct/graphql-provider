package org.babyfish.graphql.provider.starter.runtime.expression

import org.babyfish.graphql.provider.starter.runtime.query.DatabaseSubQuery
import org.babyfish.graphql.provider.starter.runtime.query.TypedDatabaseSubQuery
import kotlin.reflect.KClass

fun <X: Any> sql(
    type: KClass<X>,
    sql: String,
    block: SqlExpressionContext.() -> Unit
): Expression<X> {
    TODO()
}

class SqlExpressionContext {
    fun expressions(vararg expressions: Expression<*>) {}
    fun values(vararg value: Any?) {}
}

@Suppress("UNCHECKED_CAST")
fun and(
    vararg predicates: Expression<Boolean>?
): Expression<Boolean>? =
    combine("and", predicates as Array<Expression<Boolean>?>)

@Suppress("UNCHECKED_CAST")
fun or(
    vararg predicates: Expression<Boolean>?
): Expression<Boolean>? =
    combine("or", predicates as Array<Expression<Boolean>?>)

private fun combine(
    separator: String,
    predicates: Array<Expression<Boolean>?>
): Expression<Boolean>? =
    predicates
        .filterNotNull()
        .takeIf { it.isNotEmpty() }
        ?.let { list ->
            list
                .takeIf { it.size == 1 }
                ?.let { list.first() }
                ?: CombinedExpression(separator, list)
        }


fun not(
    predicate: Expression<Boolean>?
): Expression<Boolean>? =
    predicate?.let {
        NotExpression(it)
    }

infix fun Expression<String>.like(
    value: String
): Expression<Boolean> =
    like(value, LikeMode.ANYWHERE)

fun Expression<String>.like(
    value: String,
    likeMode: LikeMode
): Expression<Boolean> =
    LikeExpression(this, value, false, likeMode)


infix fun Expression<String>.ilike(
    value: String
): Expression<Boolean> =
    ilike(value, LikeMode.ANYWHERE)

fun Expression<String>.ilike(
    value: String,
    likeMode: LikeMode
): Expression<Boolean> =
    LikeExpression(this, value, true, likeMode)


infix fun <T: Comparable<T>> Expression<T>.eq(
    value: T
): Expression<Boolean> = eq(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.eq(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("=", this, other)


infix fun <T: Comparable<T>> Expression<T>.ne(
    value: T
): Expression<Boolean> =
    ne(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.ne(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("<>", this, other)


infix fun <T: Comparable<T>> Expression<T>.lt(
    value: T
): Expression<Boolean> =
    lt(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.lt(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("<", this, other)


infix fun <T: Comparable<T>> Expression<T>.le(
    value: T
): Expression<Boolean> =
    le(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.le(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression("<=", this, other)


infix fun <T: Comparable<T>> Expression<T>.gt(
    value: T
): Expression<Boolean> =
    gt(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.gt(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression(">", this, other)


infix fun <T: Comparable<T>> Expression<T>.ge(
    value: T
): Expression<Boolean> =
    ge(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.ge(
    other: Expression<T>
): Expression<Boolean> =
    ComparisonExpression(">=", this, other)


fun <T: Comparable<T>> Expression<T>.between(
    min: T,
    max: T
): Expression<Boolean> =
    between(ValueExpression(min), ValueExpression(max))

fun <T: Comparable<T>> Expression<T>.between(
    min: Expression<T>,
    max: Expression<T>
): Expression<Boolean> =
    BetweenExpression(this, min, max)


val Expression<*>.isNull: Expression<*>
    get() = NullityExpression(true, this)

val Expression<*>.isNotNull: Expression<*>
    get() = NullityExpression(false, this)

fun <A, B> tuple(
    a: Expression<A>,
    b: Expression<B>
): Expression<Pair<A, B>> =
    PairExpression(a, b)

fun <A, B, C> tuple(
    a: Expression<A>,
    b: Expression<B>,
    c: Expression<C>
): Expression<Triple<A, B, C>> =
    TripleExpression(a, b, c)

infix fun <T> Expression<T>.valueIn(
    values: Collection<T>
): Expression<Boolean> =
    InListExpression(false, this, values)

infix fun <T> Expression<T>.valueNotIn(
    values: Collection<T>
): Expression<Boolean> =
    InListExpression(true, this, values)


infix fun <T> Expression<T>.valueIn(
    subQuery: TypedDatabaseSubQuery<*, *, T>
): Expression<Boolean> =
    InSubQueryExpression(false, this, subQuery)

infix fun <T> Expression<T>.valueNotIn(
    subQuery: TypedDatabaseSubQuery<*, *, T>
): Expression<Boolean> =
    InSubQueryExpression(true, this, subQuery)


fun exists(
    subQuery: DatabaseSubQuery<*, *>
): Expression<Boolean> =
    ExistsExpression(false, subQuery)

fun notExists(
    subQuery: DatabaseSubQuery<*, *>
): Expression<Boolean> =
    ExistsExpression(true, subQuery)


fun <T: Number> constant(value: T): Expression<T> =
    ConstantExpression(value)