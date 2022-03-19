package org.babyfish.graphql.provider.runtime.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.dataloader.MappedBatchLoader
import reactor.core.publisher.Mono
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

internal class ManyToManyBatchLoader(
    private val r2dbcClient: R2dbcClient,
    private val prop: ModelProp
) : MappedBatchLoader<Any, List<Any>> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, List<Any>>> =
        mono(Dispatchers.Unconfined) {
            loadImpl(keys)
        }.toFuture()

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadImpl(
        keys: Set<Any>
    ): Map<Any, List<Any>> {
        val pairs = loadIdPairs(keys)
        val idMap = pairs.groupBy({it.first!!}) {
            it.second!!
        }
        val allTargetIds = pairs.map { it.second }.distinct()
        val rows =
            r2dbcClient.query(
                prop.targetType!!.kotlinType as KClass<Entity<FakeID>>
            ) {
                where { table.id valueIn allTargetIds as Collection<FakeID> }
                select(table)
            } as List<Entity<*>>
        val rowMap = rows.associateBy { it.id }
        return idMap.mapValues { e ->
            e.value.distinct().mapNotNull { rowMap[it] }
        }
    }

    private suspend fun loadIdPairs(keys: Set<Any>): List<Pair<Any?, Any?>> {
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
        return result
            .map { row, _ ->
                row.get(0) to row.get(1)
            }
            .asFlow()
            .toList()
    }
}
