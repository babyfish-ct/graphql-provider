package org.babyfish.graphql.provider.r2dbc

import io.r2dbc.spi.Batch
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Statement

internal class ConnectionProxy(
    private val target: Connection
) : Connection by target {

    override fun createStatement(sql: String): Statement =
        StatementProxy(target.createStatement(sql))

    override fun createBatch(): Batch =
        BatchProxy(target.createBatch())
}