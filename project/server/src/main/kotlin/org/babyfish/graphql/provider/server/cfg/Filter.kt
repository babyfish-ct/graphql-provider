package org.babyfish.graphql.provider.server.cfg

import org.babyfish.graphql.provider.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

data class Arg(
    val name: String,
    val type: KClass<*>,
    val required: Boolean = false
)

interface Filter<T: Immutable> {
    val ctx: Any
    val args: Args
    val query: Query<T>
}

interface Args {

    fun string(name: String): String?
    fun string(name: String, requiredMessage: String): String

    fun int(name: String): Int?
    fun int(name: String, requiredMessage: String): Int

    fun long(name: String): Long?
    fun long(name: String, requiredMessage: String): Long

    fun float(name: String): Float?
    fun float(name: String, requiredMessage: String): Float

    fun double(name: String): Double?
    fun double(name: String, requiredMessage: String): Double

    fun <T: Any> get(name: String, type: KClass<T>): T?
    fun <T: Any> get(name: String, type: KClass<T>, requiredMessage: String): T
}

interface Query<T: Immutable> {
    val table: From<T>
    fun addCondition(expression: Expression<Boolean>)
    fun and(vararg expressions: Expression<Boolean>): Expression<Boolean>
    fun or(vararg expressions: Expression<Boolean>): Expression<Boolean>
    fun orderBy(expression: Expression<*>, descending: Boolean = false)
    fun not(vararg expressions: Expression<Boolean>): Expression<Boolean>
    fun like(expression: Expression<String?>, value: String): Expression<Boolean>
    fun <X: Comparable<X>> eq(expression: Expression<X>, value: X): Expression<Boolean>
    fun <X: Comparable<X>> ne(expression: Expression<X>, value: X): Expression<Boolean>
    fun <X: Comparable<X>> gt(expression: Expression<X>, value: X): Expression<Boolean>
    fun <X: Comparable<X>> ge(expression: Expression<X>, value: X): Expression<Boolean>
    fun <X: Comparable<X>> lt(expression: Expression<X>, value: X): Expression<Boolean>
    fun <X: Comparable<X>> le(expression: Expression<X>, value: X): Expression<Boolean>
    fun <X: Any> sql(type: KClass<X>, sql: String, expressions: Array<Expression<*>>, values: Array<Any>): Expression<X>
}

interface Expression<T> {

}

interface From<T: Immutable> {
    fun <X: Immutable> reference(prop: KProperty1<T, X>): From<X>
    fun <X: Immutable> list(prop: KProperty1<T, List<X>>): From<X>
    operator fun <X> get(prop: KProperty1<T, X?>): Expression<X>
}