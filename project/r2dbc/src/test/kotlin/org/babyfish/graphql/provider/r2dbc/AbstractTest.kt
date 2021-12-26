package org.babyfish.graphql.provider.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactories
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.babyfish.graphql.provider.r2dbc.proxy.ConnectionFactoryProxy
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class AbstractTest(
    private val dbName: String? = null
) {

    private var _con: Connection? = null

    @BeforeTest
    fun setup() {
        runBlocking {
            val cf =
                ConnectionFactoryProxy(ConnectionFactories.get("r2dbc:h2:mem:///${dbName ?: this::class.simpleName}"))
            _con = cf.create().awaitSingle()
            initializeData()
        }
    }

    @AfterTest
    fun teardown() {
        _con?.close()
        _con = null
    }

    protected val connection: Connection
        get() = _con ?: error("No connection")

    protected open suspend fun initializeData() {}
}