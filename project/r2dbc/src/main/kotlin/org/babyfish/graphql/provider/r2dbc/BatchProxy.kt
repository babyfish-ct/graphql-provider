package org.babyfish.graphql.provider.r2dbc

import io.r2dbc.spi.Batch

internal class BatchProxy(
    private val target: Batch
) : Batch by target {

    override fun add(sql: String): BatchProxy {
        target.add(sql)
        return this
    }
}