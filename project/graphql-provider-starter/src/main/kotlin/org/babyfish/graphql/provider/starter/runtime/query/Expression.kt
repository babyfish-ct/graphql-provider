package org.babyfish.graphql.provider.starter.runtime.query

import org.babyfish.graphql.provider.starter.meta.EntityProp

interface Expression<T>

internal abstract class AbstractExpression<T>: Expression<T>, Renderable {

    protected abstract fun SqlBuilder.render()

    final override fun renderTo(builder: SqlBuilder) {
        builder.render()
    }

    /*
     * Copy from SQL server documentation
     *
     * 1 ~ (Bitwise NOT)
     * 2 * (Multiplication), / (Division), % (Modulus)
     * 3 + (Positive), - (Negative), + (Addition), + (Concatenation), - (Subtraction), & (Bitwise AND), ^ (Bitwise Exclusive OR), | (Bitwise OR)
     * 4 =, >, <, >=, <=, <>, !=, !>, !< (Comparison operators)
     * 5 NOT
     * 6 AND
     * 7 ALL, ANY, BETWEEN, IN, LIKE, OR, SOME
     * 8 = (Assignment)
     */
    protected abstract val precedence: Int

    protected fun SqlBuilder.render(expression: Expression<*>) {
        (expression as Renderable).let {
            if (it !is AbstractExpression<*> || it.precedence <= precedence) {
                it.renderTo(this)
            } else {
                sql("(")
                it.renderTo(this)
                sql(")")
            }
        }
    }

    protected fun SqlBuilder.lowestPrecedence(
        withBrackets: Boolean,
        block: LowestPrecedenceContext.() -> Unit
    ) {
        if (withBrackets) {
            sql("(")
            LowestPrecedenceContext(this).block()
            sql(")")
        } else {
            LowestPrecedenceContext(this).block()
        }
    }

    protected inner class LowestPrecedenceContext(
        private val builder: SqlBuilder
    ) {
        fun render(expression: Expression<*>) {
            (expression as Renderable).renderTo(builder)
        }
    }
}

internal class PropExpression<T>(
    val table: TableImpl<*>,
    val prop: EntityProp
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        if (prop.isId && table.parentProp !== null) {
            val middleTable = table.parentProp.middleTable ?: table.parentProp?.mappedBy?.middleTable
            val inverse = table.parentProp.mappedBy !== null
            if (middleTable !== null) {
                sql(table.middleTableAlias!!)
                sql(".")
                sql(if (inverse) {
                    middleTable.joinColumnName
                } else {
                    middleTable.targetJoinColumnName
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
    private val operator: String,
    private val predicates: List<Expression<*>>
) : AbstractExpression<Boolean>() {

    init {
        predicates
            .takeIf { it.size > 1 }
            ?: error("Internal bug: the size of 'CombinedExpression.expressions' is not greater than 1")
    }

    override val precedence: Int
        get() = if (operator == "and") {
            6
        } else {
            7
        }

    override fun SqlBuilder.render() {
        var sp: String? = null
        for (predicate in predicates) {
            if (sp !== null) {
                sql(sp)
            } else {
                sp = " $operator "
            }
            render(predicate)
        }
    }
}

internal class NotExpression(
    private val predicate: Expression<Boolean>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 5

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

    override val precedence: Int
        get() = 7

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

    override val precedence: Int
        get() = 4

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

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        render(expression)
        sql(" between ")
        render(min)
        sql(" and ")
        render(max)
    }
}

internal class BinaryExpression<T: Number>(
    private val operator: String,
    private val left: Expression<T>,
    private val right: Expression<T>
): AbstractExpression<T>() {

    override val precedence: Int
        get() = when (operator) {
            "*", "/", "%" -> 2
            else -> 3
        }
    override fun SqlBuilder.render() {
        render(left)
        sql(" ")
        sql(operator)
        sql(" ")
        render(right)
    }
}

internal class UnaryExpression<T: Number>(
    private val operator: String,
    private val target: Expression<T>
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 3

    override fun SqlBuilder.render() {
        sql(operator)
        render(target)
    }
}

internal class NullityExpression(
    private val isNull: Boolean,
    private val expression: Expression<*>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 0

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

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        lowestPrecedence(true) {
            render(a)
            sql(", ")
            render(b)
        }
    }
}

internal class TripleExpression<A, B, C>(
    private val a: Expression<A>,
    private val b: Expression<B>,
    private val c: Expression<C>
): AbstractExpression<Triple<A, B, C>>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {

        lowestPrecedence(true) {
            render(a)
            sql(", ")
            render(b)
            sql(", ")
            render(c)
        }
    }
}

internal class InListExpression<T>(
    private val negative: Boolean,
    private val expression: Expression<T>,
    private val values: Collection<T>
): AbstractExpression<Boolean>() {

    override val precedence: Int
        get() = 7

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

    override val precedence: Int
        get() = 7

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

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        sql(if (negative) "not " else "")
        sql("exists(select *")
        AbstractQuery.form(subQuery).renderWithoutSelection(this)
        sql(")")
    }
}

internal class OperatorSubQueryExpression<T>(
    private val operator: String,
    private val subQuery: TypedDatabaseSubQuery<*, *, T>
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 7

    override fun SqlBuilder.render() {
        sql(operator)
        render(subQuery)
    }
}

internal class ValueExpression<T>(
    val value: T
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        variable(value)
    }
}

internal class ConstantExpression<T: Number>(
    private val value: T
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        if (value::class == String::class) {
            error("In order to avoid injection attack, constant expression is not supported for string")
        }
        sql(value.toString())
    }
}

internal class AggregationExpression<T>(
    private val funName: String,
    private val base: Expression<*>,
    private val prefix: String? = null
): AbstractExpression<T>() {

    override val precedence: Int
        get() = 0

    override fun SqlBuilder.render() {
        sql(funName)
        lowestPrecedence(true) {
            prefix?.let {
                sql(it);
                sql(" ")
            }
            render(base)
        }
    }
}