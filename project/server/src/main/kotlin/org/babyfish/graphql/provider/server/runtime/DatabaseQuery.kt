package org.babyfish.graphql.provider.server.runtime

import org.babyfish.graphql.provider.server.runtime.expression.Expression
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
        block: DatabaseSubQuery<T, X, Unit>.() -> Unit
    ): DatabaseSubQuery<T, X, Unit>

    fun <X: Immutable, R> subQuery(
        type: KClass<X>,
        selectionExpressionSupplier: (JoinableTable<X>) -> Expression<R>,
        block: DatabaseSubQuery<T, X, R>.() -> Unit
    ): DatabaseSubQuery<T, X, R>

    fun <X: Immutable, R> subQuery(
        type: KClass<X>,
        selectionProp: KProperty1<X, R>,
        block: DatabaseSubQuery<T, X, R>.() -> Unit
    ): DatabaseSubQuery<T, X, R> =
        subQuery(type, { it[selectionProp] }, block)
}