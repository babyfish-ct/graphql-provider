package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.QueryProp
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.meta.config.Column
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.dataloader.MappedBatchLoader
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal open class DataFetchers(
    private val r2dbcClient: R2dbcClient
) {
    @Suppress("UNCHECKED_CAST")
    suspend fun fetch(prop: QueryProp, env: DataFetchingEnvironment): Any? =
        r2dbcClient.execute(
            prop.targetType!!.kotlinType as KClass<Entity<FakeID>>,
        ) {
            prop.filter.execute(env, FilterExecutionContext(this, mutableSetOf()))
            select(table)
        }

    suspend fun fetch(prop: ModelProp, env: DataFetchingEnvironment): Any? {
        val entity = env.getSource<Entity<*>>()
        if (prop.isReference && prop.storage is Column && Immutable.isLoaded(entity, prop.immutableProp)) {
            val parent = Immutable.get(entity, prop.immutableProp) as Entity<*>?
            if (parent === null) {
                return null
            }
            val parentId = Immutable.get(parent, prop.targetType!!.idProp.immutableProp)
            if (env.arguments.isEmpty()) {
                val fields = env.selectionSet.fields
                if (fields.size == 1 && fields[0].name == "id") {
                    return produce(prop.targetType!!.kotlinType) {
                        Draft.set(this, prop.targetType!!.idProp.immutableProp, parentId)
                    }
                }
            }
            return env.loaderByParentId(prop).load(parentId).await()
        } else {
            val list = env.loaderById(prop).load(entity.id).await()
            if (prop.isReference) {
                return if (list.isNullOrEmpty()) null else list[0]
            }
            return list ?: emptyList<Any>()
        }
    }

    private fun DataFetchingEnvironment.loaderByParentId(prop: ModelProp): DataLoader<Any, Any?> {
        val dataLoaderKey = "graphql-provider:loader-by-parent-id:${prop}"
        return dataLoaderRegistry.computeIfAbsent(dataLoaderKey) {
            DataLoaderFactory.newMappedDataLoader(
                LoaderByParentId(prop),
                DataLoaderOptions().setMaxBatchSize(64)
            )
        }
    }

    private inner class LoaderByParentId(
        private val prop: ModelProp
    ) : MappedBatchLoader<Any, Any> {

        override fun load(
            keys: Set<Any>
        ): CompletionStage<Map<Any, Any>> =
            mono(Dispatchers.Unconfined) {
                loadImpl(keys)
            }.toFuture()

        @Suppress("UNCHECKED_CAST")
        private suspend fun loadImpl(
            keys: Set<Any>
        ): Map<Any, Any> {
            val rows = r2dbcClient.execute(
                prop.targetType!!.kotlinType as KClass<Entity<FakeID>>
            ) {
                where { table.id valueIn keys as Collection<FakeID> }
                select(table)
            } as List<Entity<*>>
            return rows.associateBy {
                it.id
            }
        }
    }

    private fun DataFetchingEnvironment.loaderById(prop: ModelProp): DataLoader<Any, List<Any>> {
        val dataLoaderKey = "graphql-provider:loader-by-id:${prop}"
        return dataLoaderRegistry.computeIfAbsent(dataLoaderKey) {
            DataLoaderFactory.newMappedDataLoader(
                LoaderById(prop),
                DataLoaderOptions().setMaxBatchSize(64)
            )
        }
    }

    private inner class LoaderById(
        private val prop: ModelProp
    ) : MappedBatchLoader<Any, List<Any>> {

        override fun load(
            keys: Set<Any>
        ): CompletionStage<Map<Any, List<Any>>> =
            mono(Dispatchers.Unconfined) {
                loadImpl(keys)
            }.toFuture()

        private suspend fun loadImpl(
            keys: Set<Any>
        ): Map<Any, List<Any>> =
            if (prop.isReference || prop.opposite?.isReference == true) {
                // many-to-one || one-to-many || one-to-one
                loadWithJoin(keys)
            } else {
                // many-to-many
                loadWithoutJoin(keys)
            }

        @Suppress("UNCHECKED_CAST")
        private suspend fun loadWithJoin(
            keys: Set<Any>
        ): Map<Any, List<Any>> {
            val rows = r2dbcClient.execute(prop.targetType!!.kotlinType as KClass<Entity<FakeID>>) {
                val joinedTable = when {
                    prop.isConnection -> table.`←joinConnection`(
                        prop.kotlinProp as KProperty1<Entity<FakeID>, Connection<Entity<FakeID>>>
                    )
                    prop.isList -> table.`←joinList`(
                        prop.kotlinProp as KProperty1<Entity<FakeID>, List<Entity<FakeID>>>
                    )
                    prop.isReference -> table.`←joinReference`(
                        prop.kotlinProp as KProperty1<Entity<FakeID>, Entity<FakeID>?>
                    )
                    else -> error("Internal bug")
                }
                where { joinedTable.id valueIn keys as Collection<FakeID> }
                select {
                    joinedTable.id then table
                }
            } as List<Pair<Any, Entity<*>>>
            return rows.groupBy({ it.first }) {
                it.second
            }
        }

        @Suppress("UNCHECKED_CAST")
        private suspend fun loadWithoutJoin(
            keys: Set<Any>
        ): Map<Any, List<Any>> {
            val (tableName, thisColumn, targetColumn) =
                (prop.storage as? MiddleTable)
                    ?.let {
                        Triple(it.tableName, it.joinColumnName, it.targetJoinColumnName)
                    }
                    ?: (prop.opposite?.storage as? MiddleTable)
                        ?.let {
                            Triple(it.tableName, it.targetJoinColumnName, it.joinColumnName)
                        } ?: error("Internal bug, middle table is expected")
            val result =
                r2dbcClient.databaseClient.inConnection { con ->
                    val list = keys.indices.joinToString { "$${it + 1}" }
                    val sql = "select $thisColumn, $targetColumn from $tableName " +
                        "where $thisColumn in ($list)"
                    val statement = con.createStatement(sql)
                    for ((index, key) in keys.withIndex()) {
                        statement.bind(index, key)
                    }
                    Mono.from(statement.execute())
                }.awaitSingle()
            var pairs = result
                .map { row, _ ->
                    row.get(0) to row.get(1)
                }
                .asFlow()
                .toList()
            val idMap = pairs.groupBy({it.first!!}) {
                it.second!!
            }
            val allTargetIds = pairs.map { it.second }.distinct()
            val rows =
                r2dbcClient.execute(
                    prop.targetType!!.kotlinType as KClass<Entity<FakeID>>
                ) {
                    where { table.id valueIn allTargetIds as Collection<FakeID> }
                    select(table)
                } as List<Entity<*>>
            val rowMap = rows.associateBy { it.id }
            println("idMap: $idMap")
            println("rowMap: $rowMap")
            return idMap.mapValues { e ->
                e.value.distinct().mapNotNull { rowMap[it] }
            }.also {
                println("resultMap: $it")
            }
        }
    }

    private interface FakeID: Comparable<FakeID>
}