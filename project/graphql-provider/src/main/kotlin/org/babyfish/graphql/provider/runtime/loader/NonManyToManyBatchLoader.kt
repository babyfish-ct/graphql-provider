package org.babyfish.graphql.provider.runtime.loader

import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.runtime.FakeID
import org.babyfish.graphql.provider.runtime.R2dbcClient
import org.babyfish.graphql.provider.security.executeWithSecurityContext
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.valueIn
import org.dataloader.MappedBatchLoader
import org.springframework.security.core.context.SecurityContext
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class NonManyToManyBatchLoader(
    private val r2dbcClient: R2dbcClient,
    private val prop: ModelProp,
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
        val rows = r2dbcClient.query(prop.targetType!!.kotlinType as KClass<Entity<FakeID>>) {
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
            filterApplier(this)
            select {
                joinedTable.id then table
            }
        } as List<Pair<Any, Entity<*>>>
        return rows.groupBy({ it.first }) {
            it.second
        }
    }
}