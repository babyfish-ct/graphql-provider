package org.babyfish.graphql.provider.server.runtime.query

import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.graphql.provider.server.runtime.DatabaseQuery
import org.babyfish.graphql.provider.server.runtime.DatabaseSubQuery
import org.babyfish.graphql.provider.server.runtime.JoinableTable
import org.babyfish.graphql.provider.server.runtime.expression.Expression
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass

internal abstract class AbstractQuery<T: Immutable>(
    val tableAliasAllocator: TableAliasAllocator,
    val entityTypeMap: Map<ImmutableType, EntityType>,
    type: KClass<T>
): DatabaseQuery<T>, Renderable {

    private val predicates = mutableListOf<Expression<Boolean>>()

    private val orders = mutableListOf<Order>()

    override val table: TableImpl<T> = TableImpl(
        this.let { it }, // Boring code ".let{ it }" is used to avoid compilation warning
        entityTypeMap[ImmutableType.Companion.of(type)]
            ?: throw IllegalArgumentException("Cannot create query for unmapped type '${type.qualifiedName}'")
    )

    override fun where(vararg predicates: Expression<Boolean>?) {
        for (predicate in predicates) {
            predicate?.let {
                this.predicates += it
            }
        }
    }

    override fun orderBy(expression: Expression<*>?, descending: Boolean) {
        expression?.let {
            orders += Order(expression, descending)
        }
    }

    override fun <X : Immutable> subQuery(
        type: KClass<X>,
        block: DatabaseSubQuery<T, X, Unit>.() -> Unit
    ): DatabaseSubQuery<T, X, Unit> =
        SubQueryImpl<T, X, Unit>(this, type, null).apply {
            block
        }

    override fun <X : Immutable, R> subQuery(
        type: KClass<X>,
        selectionExpressionSupplier: (JoinableTable<X>) -> Expression<R>,
        block: DatabaseSubQuery<T, X, R>.() -> Unit
    ): DatabaseSubQuery<T, X, R> =
        SubQueryImpl(this, type, selectionExpressionSupplier).apply {
            block
        }

    protected fun SqlBuilder.renderWithoutSelection() {
        (table as Renderable).apply {
            render()
        }
        if (predicates.isNotEmpty()) {
            sql(" where ")
            var separator: String? = null
            for (predicate in predicates) {
                if (separator === null) {
                    separator = " and "
                } else {
                    sql(separator)
                }
                (predicate as Renderable).apply {
                    render()
                }
            }
        }
        if (orders.isNotEmpty()) {
            sql(" order by ")
            var separator: String? = null
            for (order in orders) {
                if (separator === null) {
                    separator = ", "
                } else {
                    sql(separator)
                }
                (order as Renderable).apply {
                    render()
                }
            }
        }
    }
}