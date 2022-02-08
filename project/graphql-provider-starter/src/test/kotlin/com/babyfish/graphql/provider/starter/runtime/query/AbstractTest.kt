//package com.babyfish.graphql.provider.starter.runtime.query
//
//import com.babyfish.graphql.provider.starter.mapper.AuthorMapper
//import com.babyfish.graphql.provider.starter.mapper.BookMapper
//import com.babyfish.graphql.provider.starter.mapper.BookRepository
//import com.babyfish.graphql.provider.starter.mapper.BookStoreMapper
//import com.babyfish.graphql.provider.starter.query.AuthorQuery
//import com.babyfish.graphql.provider.starter.query.BookQuery
//import com.babyfish.graphql.provider.starter.query.BookStoreQuery
//import org.babyfish.graphql.provider.starter.meta.EntityType
//import org.babyfish.graphql.provider.starter.runtime.query.DatabaseQuery
//import org.babyfish.kimmer.Immutable
//import org.babyfish.kimmer.meta.ImmutableType
//import kotlin.reflect.KClass
//import kotlin.reflect.KProperty1
//import kotlin.reflect.full.memberProperties
//import kotlin.reflect.full.primaryConstructor
//import kotlin.test.fail
//
//abstract class AbstractTest {
//
//    companion object {
//
//        @JvmStatic
//        @Suppress("UNCHECKED_CAST")
//        protected fun <T: Immutable> query(
//            type: KClass<T>
//        ): DatabaseQuery<T> =
//            databaseQueryConstructor.call(entityTypes, type) as DatabaseQuery<T>
//
//        @JvmStatic
//        private val entityTypes: Map<ImmutableType, EntityType> =
//            entityTypes()
//
//        @JvmStatic
//        @Suppress("UNCHECKED_CAST")
//        private fun entityTypes(): Map<ImmutableType, EntityType> {
//
//            val generatorType =
//                Class.forName(
//                    "com.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator"
//                ).kotlin
//
//            val entityTypeProps =
//                generatorType
//                    .memberProperties
//                    .find { it.name === "entityTypes" }
//                    as KProperty1<Any, Map<ImmutableType, EntityType>>?
//                    ?: fail("Cannot get entityTypes property")
//
//            val generator = generatorType.primaryConstructor!!.call(
//                listOf(BookStoreQuery(), BookQuery(), AuthorQuery()),
//                listOf(BookStoreMapper(BookRepository()), BookMapper(), AuthorMapper())
//            )
//
//            return entityTypeProps.get(generator)
//        }
//
//        private val databaseQueryConstructor = Class.forName(
//            "com.babyfish.graphql.provider.starter.runtime.query.QueryImpl"
//        ).kotlin.primaryConstructor!!
//    }
//}