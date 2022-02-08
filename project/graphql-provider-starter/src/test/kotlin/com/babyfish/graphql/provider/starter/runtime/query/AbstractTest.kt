package com.babyfish.graphql.provider.starter.runtime.query

import com.babyfish.graphql.provider.starter.mapper.AuthorMapper
import com.babyfish.graphql.provider.starter.mapper.BookMapper
import com.babyfish.graphql.provider.starter.mapper.BookRepository
import com.babyfish.graphql.provider.starter.mapper.BookStoreMapper
import com.babyfish.graphql.provider.starter.query.AuthorQuery
import com.babyfish.graphql.provider.starter.query.BookQuery
import com.babyfish.graphql.provider.starter.query.BookStoreQuery
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.Query
import org.babyfish.graphql.provider.starter.runtime.query.DatabaseQuery
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.fail

abstract class AbstractTest {

    fun <T: Immutable> query(
        type: KClass<T>
    ): DatabaseQuery<T> =
        query(
            listOf(BookStoreQuery(), BookQuery(), AuthorQuery()),
            listOf(BookStoreMapper(BookRepository()), BookMapper(), AuthorMapper()),
            type
        )

    companion object {

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T: Immutable> query(
            queries: List<Query>,
            mappers: List<EntityMapper<*>>,
            type: KClass<T>
        ): DatabaseQuery<T> =
            queryFun.call(testHelperType.companionObjectInstance!!, queries, mappers, type) as DatabaseQuery<T>

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun render(query: DatabaseQuery<*>): Pair<String, List<Any?>> =
            renderFun.call(testHelperType.companionObjectInstance!!, query) as Pair<String, List<Any?>>

        private val testHelperType =
            Class.forName(
                "org.babyfish.graphql.provider.starter.runtime.TestHelper"
            ).kotlin

        private val queryFun =
            testHelperType.companionObject!!.memberFunctions
                .find { it.name == "query" }
                ?: fail("No TestHelper.query")

        private val renderFun =
            testHelperType.companionObject!!.memberFunctions
                .find { it.name == "render" }
                ?: fail("No TestHelper.render")
    }
}
