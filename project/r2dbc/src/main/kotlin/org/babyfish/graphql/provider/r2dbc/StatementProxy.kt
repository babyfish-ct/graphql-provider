package org.babyfish.graphql.provider.r2dbc

import io.r2dbc.spi.Statement

internal class StatementProxy(
    private val target: Statement
) : Statement by target {


}