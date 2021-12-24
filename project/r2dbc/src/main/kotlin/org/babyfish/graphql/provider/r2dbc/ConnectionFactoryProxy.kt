package org.babyfish.graphql.provider.r2dbc

import io.r2dbc.spi.Connection
import io.r2dbc.spi.ConnectionFactory
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

class ConnectionFactoryProxy(
    private val target: ConnectionFactory
) : ConnectionFactory by target {

    override fun create(): Publisher<out Connection> =
        Mono.from(target.create()).map {
            ConnectionProxy(it)
        }
}