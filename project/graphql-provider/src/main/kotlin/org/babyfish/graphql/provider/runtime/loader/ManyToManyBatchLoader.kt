package org.babyfish.graphql.provider.runtime.loader

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.executeWithSecurityContext
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.valueIn
import org.babyfish.kimmer.sql.meta.config.MiddleTable
import org.dataloader.MappedBatchLoader
import org.springframework.security.core.context.SecurityContext
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

internal class ManyToManyBatchLoader(
    private val r2dbcClient: R2dbcClient,
    private val prop: ModelProp,
    private val idOnly: Boolean,
    private val securityContext: SecurityContext?,
    private val filterApplier: (MutableRootQuery<Entity<FakeID>, FakeID>) -> Unit
) : MappedBatchLoader<Any, List<Any>> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, List<Any>>> =
        executeWithSecurityContext(securityContext) {
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
        if (idOnly && prop.filter === null) {
            val targetType = prop.targetType!!
            return idMap.mapValues { entry ->
                entry.value.map {
                    produce(targetType.kotlinType) {
                        Draft.set(this, targetType.idProp.immutableProp, it)
                    }
                }
            }
        }
        val allTargetIds = pairs.map { it.second }.distinct()
        val rows =
            r2dbcClient.query(
                prop.targetType!!.kotlinType as KClass<Entity<FakeID>>
            ) {
                where { table.id valueIn allTargetIds as Collection<FakeID> }
                filterApplier(this)
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
        val dialect = r2dbcClient.sqlClient.dialect
        val sql = "select $thisColumn, $targetColumn from $tableName " +
            "where $thisColumn in (${
                keys.indices.joinToString { dialect.r2dbcParameter(it + 1) }
            })"
        return r2dbcClient.execute {
            // TODO: kimmer-sql API bad design: List -> Collection
            r2dbcClient.sqlClient.r2dbcExecutor.execute(it, sql, keys) {
                map { row, _ ->
                    row.get(0) to row.get(1)
                }.asFlow().toList()
            }
        }
    }
}
