package org.babyfish.graphql.provider.runtime.loader

import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.executeWithSecurityContext
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.valueIn
import org.dataloader.MappedBatchLoader
import org.springframework.security.core.context.SecurityContext
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

internal class BatchLoaderByParentId(
    private val r2dbcClient: R2dbcClient,
    private val prop: ModelProp,
    private val securityContext: SecurityContext?,
    private val filterApplier: (MutableRootQuery<Entity<FakeID>, FakeID>) -> Unit
) : MappedBatchLoader<Any, Any> {

    override fun load(
        keys: Set<Any>
    ): CompletionStage<Map<Any, Any>> =
        executeWithSecurityContext(securityContext) {
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