package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.dsl.ArgumentType
import kotlin.reflect.KClass

interface Expression<T>


fun <X: Any> sql(
    type: KClass<X>,
    sql: String,
    expressions: Array<Expression<*>>,
    values: Array<Any>
): Expression<X> = TODO()

fun and(
    vararg predicates: Expression<Boolean>
): Expression<Boolean>? = TODO()

fun or(
    vararg predicates: Expression<Boolean>
): Expression<Boolean>? = TODO()

fun not(
    vararg predicates: Expression<Boolean>
): Expression<Boolean>? = TODO()


infix fun Expression<String>.like(
    value: String
): Expression<Boolean> =
    like(ValueExpression(value), LikeMode.ANYWHERE)

fun Expression<String>.like(
    value: String,
    likeMode: LikeMode
): Expression<Boolean> =
    like(ValueExpression(value), likeMode)

infix fun Expression<String>.like(
    other: Expression<String>
): Expression<Boolean> =
    like(other, LikeMode.ANYWHERE)

fun Expression<String>.like(
    other: Expression<String>,
    likeMode: LikeMode
): Expression<Boolean> = TODO()


infix fun Expression<String>.ilike(
    value: String
): Expression<Boolean> =
    ilike(ValueExpression(value), LikeMode.ANYWHERE)

fun Expression<String>.ilike(
    value: String,
    likeMode: LikeMode
): Expression<Boolean> =
    ilike(ValueExpression(value), likeMode)

infix fun Expression<String>.ilike(
    other: Expression<String>
): Expression<Boolean> =
    ilike(other, LikeMode.ANYWHERE)

fun Expression<String>.ilike(
    other: Expression<String>,
    likeMode: LikeMode
): Expression<Boolean> = TODO()


infix fun <T: Comparable<T>, X: Comparable<X>> Expression<T>.eq(
    value: X
): Expression<Boolean> = eq(ValueExpression(value))

infix fun <T: Comparable<T>, X: Comparable<X>> Expression<T>.eq(
    other: Expression<X>
): Expression<Boolean> = TODO()


infix fun <T: Comparable<T>> Expression<T>.ne(
    value: T
): Expression<Boolean> =
    ne(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.ne(
    other: Expression<T>
): Expression<Boolean> =
    TODO()


infix fun <T: Comparable<T>> Expression<T>.lt(
    value: T
): Expression<Boolean> =
    lt(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.lt(
    other: Expression<T>
): Expression<Boolean> =
    TODO()


infix fun <T: Comparable<T>> Expression<T>.le(
    value: T
): Expression<Boolean> =
    le(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.le(
    other: Expression<T>
): Expression<Boolean> =
    TODO()


infix fun <T: Comparable<T>> Expression<T>.gt(
    value: T
): Expression<Boolean> =
    gt(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.gt(
    other: Expression<T>
): Expression<Boolean> = TODO()


infix fun <T: Comparable<T>> Expression<T>.ge(
    value: T
): Expression<Boolean> =
    ge(ValueExpression(value))

infix fun <T: Comparable<T>> Expression<T>.ge(
    other: Expression<T>
): Expression<Boolean> = TODO()

internal class ValueExpression<T>(
    val value: T
): Expression<T>

internal class ArgumentExpression<T>(
    val name: String,
    val type: ArgumentType<T>
): Expression<T>