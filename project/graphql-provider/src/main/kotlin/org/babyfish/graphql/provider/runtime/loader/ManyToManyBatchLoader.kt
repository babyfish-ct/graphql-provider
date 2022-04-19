package org.babyfish.graphql.provider.runtime.loader

import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.runtime.graphqlMono
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.table.source
import org.babyfish.kimmer.sql.ast.table.target
import org.babyfish.kimmer.sql.ast.valueIn
import org.dataloader.MappedBatchLoader
import org.springframework.security.core.Authentication
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class ManyToManyBatchLoader(
    private val r2dbcClient: R2dbcClient,
    private val prop: ModelProp,
    private val idOnly: Boolean,
    private val authentication: Authentication?,
    private val filterApplier: (MutableRootQuery<Entity<FakeID>, FakeID>) -> Unit
) : MappedBatchLoader<Any, List<Any>> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, List<Any>>> =
        graphqlMono(prop, null, authentication) {
            loadImpl(keys)
        }.toFuture()

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadImpl(
        keys: Set<Any>
    ): Map<Any, List<Any>> {
        val pairs = loadIdPairs(keys)
        val idMap = pairs.groupBy({it.first}) {
            it.second
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

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadIdPairs(keys: Set<Any>): List<Pair<Any, Any>> =
        r2dbcClient.execute {
            queries.byList(
                prop.kotlinProp as KProperty1<Entity<FakeID>, List<Entity<FakeID>>>
            ) {
                where(
                    (table.source.id as Expression<Any>) valueIn keys
                )
                select {
                    table.source.id then table.target.id
                }
            }.execute(it)
        }
}
