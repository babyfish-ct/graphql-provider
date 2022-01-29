package org.babyfish.graphql.provider.server.runtime.expression

import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.runtime.query.*
import org.babyfish.graphql.provider.server.runtime.query.Renderable
import org.babyfish.graphql.provider.server.runtime.query.SqlBuilder
import org.babyfish.graphql.provider.server.runtime.query.TableImpl

interface Expression<T>

internal abstract class AbstractExpression<T>: Expression<T>, Renderable {

    companion object {

        @JvmStatic
        protected fun SqlBuilder.render(expression: Expression<*>) {
            (expression as Renderable).apply {
                render()
            }
        }
    }
}

internal class PropExpression<T>(
    val table: TableImpl<*>,
    val prop: EntityProp
): AbstractExpression<T>() {

    override fun SqlBuilder.render() {
        if (prop.isId && table.parentProp !== null) {
            val middleTable = table.parentProp.middleTable
            val inverse = table.parentProp.mappedBy !== null
            if (middleTable !== null) {
                sql(table.parent!!.middleTableAlias!!)
                sql(".")
                sql(if (inverse) {
                    middleTable.targetJoinColumnName
                } else {
                    middleTable.joinColumnName
                })
                return
            }
            if (!inverse) {
                sql(table.parent!!.alias)
                sql(".")
                sql(table.parentProp.column!!.name)
                return
            }
        }
        sql(table.alias)
        sql(".")
        sql(prop.column!!.name)
    }
}

internal class CombinedExpression(
    private val separator: String,
    private val predicates: List<Expression<*>>
) : AbstractExpression<Boolean>() {

    init {
        predicates
            .takeIf { it.size > 1 }
            ?: error("Internal bug: the size of 'CombinedExpression.expressions' is not greater than 1")
    }

    override fun SqlBuilder.render() {
        var sp: String? = null
        sql("(")
        for (predicate in predicates) {
            if (sp !== null) {
                sql(sp)
            } else {
                sp = " $separator "
            }
            render(predicate)
        }
        sql(")")
    }
}

internal class NotExpression(
    private val predicate: Expression<Boolean>
): AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        sql("not(")
        render(predicate)
        sql(")")
    }
}

internal class LikeExpression(
    private val expression: Expression<String>,
    pattern: String,
    private val insensitive: Boolean,
    mode: LikeMode
) : AbstractExpression<Boolean>() {

    private val pattern: String? =
        pattern
            .takeIf { it != "" }
            ?.let {
                it
                    .takeIf { !mode.startExact && !it.startsWith("%") }
                    ?: it
            }
            ?.let {
                it
                    .takeIf { !mode.endExact && !it.endsWith("%") }
                    ?: it
            }
            ?.let {
                it
                    .takeIf { insensitive }
                    ?.let { str -> str.lowercase() }
                    ?: it
            }

    override fun SqlBuilder.render() {
        if (pattern === null) {
            sql("1 = 1")
        } else {
            if (insensitive) {
                sql("lower(")
                render(expression)
                sql(")")
            } else {
                render(expression)
            }
            sql(" like ")
            variable(pattern)
        }
    }
}

internal class ComparisonExpression<T: Comparable<T>>(
    private val operator: String,
    private val left: Expression<T>,
    private val right: Expression<T>
) : AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        render(left)
        sql(" ")
        sql(operator)
        sql(" ")
        render(right)
    }
}

internal class BetweenExpression<T: Comparable<T>>(
    private val expression: Expression<T>,
    private val min: Expression<T>,
    private val max: Expression<T>
): AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        render(expression)
        sql(" between ")
        render(min)
        sql(" and ")
        render(max)
    }
}

internal class NullityExpression(
    private val isNull: Boolean,
    private val expression: Expression<*>
): AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        render(expression)
        if (isNull) {
            sql(" is null")
        } else {
            sql(" is not null")
        }
    }
}

internal class PairExpression<A, B>(
    private val a: Expression<A>,
    private val b: Expression<B>
): AbstractExpression<Pair<A, B>>() {

    override fun SqlBuilder.render() {
        sql("(")
        render(a)
        sql(", ")
        render(b)
        sql(")")
    }
}

internal class TripleExpression<A, B, C>(
    private val a: Expression<A>,
    private val b: Expression<B>,
    private val c: Expression<C>
): AbstractExpression<Triple<A, B, C>>() {

    override fun SqlBuilder.render() {

        sql("(")
        render(a)
        sql(", ")
        render(b)
        sql(", ")
        render(c)
        sql(")")
    }
}

internal class InListExpression<T>(
    private val negative: Boolean,
    private val expression: Expression<T>,
    private val values: Collection<T>
): AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        if (values.isEmpty()) {
            sql(if (negative) "1 = 1" else "1 = 0")
        } else {
            render(expression)
            sql(if (negative) " not in (" else " in (")
            var separator: String? = null
            for (value in values) {
                if (separator === null) {
                    separator = ", "
                } else {
                    sql(separator)
                }
                variable(value)
            }
            sql(")")
        }
    }
}

internal class InSubQueryExpression<T>(
    private val negative: Boolean,
    private val expression: Expression<T>,
    private val subQuery: TypedDatabaseSubQuery<*, *, T>
): AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        render(expression)
        sql(if (negative) " not in " else " in ")
        render(subQuery)
    }
}

internal class ExistsExpression(
    private val negative: Boolean,
    private val subQuery: DatabaseSubQuery<*, *>
): AbstractExpression<Boolean>() {

    override fun SqlBuilder.render() {
        sql(if (negative) "not exists" else "exists")
        render(subQuery.select { constant(1) })
    }
}

internal class ValueExpression<T>(
    private val value: T
): AbstractExpression<T>() {

    override fun SqlBuilder.render() {
        variable(value)
    }
}

internal class ConstantExpression<T: Number>(
    private val value: T
): AbstractExpression<T>() {

    override fun SqlBuilder.render() {
        if (value::class == String::class) {
            error("In order to avoid injection attack, constant expression is not supported for string")
        }
        sql(value.toString())
    }
}