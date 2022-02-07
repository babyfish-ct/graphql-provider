package org.babyfish.graphql.provider.r2dbc

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlin.test.Test
import kotlinx.coroutines.runBlocking

class ProxyTest : AbstractTest() {

    override suspend fun initializeData() {
        connection
            .createBatch()
            .add(
                """
                            create table person(
                                id bigint not null primary key,
                                name varchar(20) not null
                            )"""
            )
            .add("insert into person values(1, 'Jim')")
            .add("insert into person values(2, 'Kate')")
            .execute()
            .asFlow()
            .toList()
    }

    @Test
    fun test() {
        val rows = runBlocking {
            connection.createStatement("select * from person")
                .execute()
                .asFlow()
                .toList()
        }
        println(rows)
    }
}
