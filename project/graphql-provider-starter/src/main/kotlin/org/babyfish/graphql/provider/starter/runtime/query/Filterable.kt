package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface Filterable<T: Immutable> {

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

    fun <X: Immutable, R> typedSubQuery(
        type: KClass<X>,
        block: DatabaseSubQuery<T, X>.() -> TypedDatabaseSubQuery<T, X, R>
    ): TypedDatabaseSubQuery<T, X, R>
}