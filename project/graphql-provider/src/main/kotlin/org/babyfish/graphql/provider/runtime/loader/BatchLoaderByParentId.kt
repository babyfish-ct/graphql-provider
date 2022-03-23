package org.babyfish.graphql.provider.runtime.loader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.Filter
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.runtime.ArgumentsConverter
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.valueIn
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

internal class BatchLoaderByParentId(
    private val r2dbcClient: R2dbcClient,
    private val prop: ModelProp,
    private val filterApplier: (MutableRootQuery<Entity<FakeID>, FakeID>) -> Unit
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
        val rows = r2dbcClient.query(
            prop.targetType!!.kotlinType as KClass<Entity<FakeID>>
        ) {
            where { table.id valueIn keys as Collection<FakeID> }
            filterApplier(this)
            select(table)
        } as List<Entity<*>>
        return rows.associateBy {
            it.id
        }
    }
}