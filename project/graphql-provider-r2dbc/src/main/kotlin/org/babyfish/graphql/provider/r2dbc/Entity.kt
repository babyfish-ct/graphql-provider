package org.babyfish.graphql.provider.r2dbc

import java.math.BigDecimal
import kotlin.reflect.KClass

abstract class Entity<ID>(name: String, idType: Class<ID>): Table(name) {

    protected fun <ID> id(
        columnName: String,
        block: IdColumnOptions<ID>.() -> Unit
    ): Column {
        TODO()
    }

    protected fun string(
        columnName: String,
        block: GeneralColumnOptions<String>.() -> Unit
    ): Column {
        TODO()
    }

    protected fun int(
        columnName: String,
        block: GeneralColumnOptions<Int>.() -> Unit
    ): Column {
        TODO()
    }

    protected fun long(
        columnName: String,
        block: GeneralColumnOptions<Long>.() -> Unit
    ): Column {
        TODO()
    }

    protected fun float(
        columnName: String,
        block: GeneralColumnOptions<Float>.() -> Unit
    ): Column {
        TODO()
    }

    protected fun double(
        columnName: String,
        block: GeneralColumnOptions<Double>.() -> Unit
    ): Column {
        TODO()
    }

    protected fun decimal(
        columnName: String,
        block: GeneralColumnOptions<BigDecimal>.() -> Unit
    ): Column {
        TODO()
    }
}
