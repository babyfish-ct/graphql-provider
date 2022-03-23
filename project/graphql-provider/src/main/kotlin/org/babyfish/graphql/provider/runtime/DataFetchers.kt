package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.future.await
import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.meta.impl.MutationPropImpl
import org.babyfish.graphql.provider.runtime.loader.BatchLoaderByParentId
import org.babyfish.graphql.provider.runtime.loader.ManyToManyBatchLoader
import org.babyfish.graphql.provider.runtime.loader.NonManyToManyBatchLoader
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.*
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.Expression
import org.babyfish.kimmer.sql.ast.count
import org.babyfish.kimmer.sql.ast.eq
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.ast.value
import org.babyfish.kimmer.sql.meta.config.Column
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.javaMethod

open class DataFetchers(
    private val r2dbcClient: R2dbcClient,
    private val argumentsConverter: ArgumentsConverter,
    private val applicationContext: ApplicationContext
) {

    @Suppress("UNCHECKED_CAST")
    suspend fun fetch(prop: QueryProp, env: DataFetchingEnvironment): Any? =
        if (prop.isConnection) {
            fetchConnection(prop, env)
        } else {
            r2dbcClient.execute {
                val query =
                    r2dbcClient.sqlClient.createQuery(prop.targetType!!.kotlinType as KClass<Entity<FakeID>>) {
                        prop.filter.execute(
                            env,
                            FilterExecutionContext(this, mutableSetOf()),
                            argumentsConverter
                        )
                        select(table)
                    }
                if (prop.isList) {
                    query.execute(it)
                } else {
                    query.execute(it).firstOrNull()
                }
            }
        }

    suspend fun fetch(prop: ModelProp, env: DataFetchingEnvironment): Any? {
        if (prop.isConnection) {
            return fetchConnection(prop, env)
        }
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

    @Suppress("UNCHECKED_CAST")
    private suspend fun fetchConnection(
        prop: GraphQLProp,
        env: DataFetchingEnvironment
    ): Connection<*> =
        r2dbcClient.execute {
            val query = r2dbcClient.sqlClient.createQuery(prop.targetType!!.kotlinType as KClass<Entity<FakeID>>) {
                if (prop is ModelProp) {
                    val sourceId = env.getSource<Entity<*>>().id
                    val sourceTable = table
                        .`←joinConnection`(
                            prop.kotlinProp as KProperty1<Entity<FakeID>, Connection<Entity<FakeID>>>
                        )
                    where { sourceTable.id eq value(sourceId) as Expression<FakeID> }
                }
                val filter = when (prop) {
                    is QueryProp -> prop.filter
                    is ModelProp -> prop.filter
                    else -> error("Internal bug: DataFetchers can only accept QueryProp and ModelProp")
                }
                filter?.execute(
                    env,
                    FilterExecutionContext(this, mutableSetOf()),
                    argumentsConverter
                )
                select(table)
            }
            val nodeType = prop.targetType!!.kotlinType as KClass<Entity<FakeID>>
            val countOnce = AsyncOnce {
                query
                    .reselect {
                        select(table.id.count())
                    }.withoutSortingAndPaging()
                    .execute(it)
                    .first()
                    .toInt()
            }
            val (limit, offset) = env.limit(countOnce)
            val nodes = if (limit > 0) {
                query.limit(limit, offset).execute(it)
            } else {
                emptyList()
            }
            produceConnectionAsync(nodeType) {
                totalCount = countOnce.get()
                edges = nodes.mapIndexed { index, node ->
                    produceEdgeDraftAsync(nodeType) {
                        this.node = node
                        cursor = indexToCursor(offset + index)
                    }
                }
                pageInfo().apply {
                    hasPreviousPage = offset > 0
                    hasNextPage = offset + limit < countOnce.get()
                    startCursor = indexToCursor(offset)
                    endCursor = indexToCursor(offset + limit - 1)
                }
            }
        }

    private fun DataFetchingEnvironment.loaderByParentId(prop: ModelProp): DataLoader<Any, Any?> {
        val dataLoaderKey = "graphql-provider:loader-by-parent-id:${prop}"
        return dataLoaderRegistry.computeIfAbsent(dataLoaderKey) {
            DataLoaderFactory.newMappedDataLoader(
                BatchLoaderByParentId(r2dbcClient, prop) {
                    applyFilter(prop.filter, it)
                },
                DataLoaderOptions().setMaxBatchSize(64)
            )
        }
    }

    private fun DataFetchingEnvironment.loaderById(prop: ModelProp): DataLoader<Any, List<Any>> {
        val dataLoaderKey = "graphql-provider:loader-by-id:${prop}"
        return dataLoaderRegistry.computeIfAbsent(dataLoaderKey) {
            DataLoaderFactory.newMappedDataLoader(
                when {
                    prop.isReference || prop.opposite?.isReference == true ->
                        NonManyToManyBatchLoader(r2dbcClient, prop) {
                            applyFilter(prop.filter, it)
                        }
                    else ->
                        ManyToManyBatchLoader(r2dbcClient, prop) {
                            applyFilter(prop.filter, it)
                        }
                },
                DataLoaderOptions().setMaxBatchSize(64)
            )
        }
    }

    private fun DataFetchingEnvironment.applyFilter(filter: Filter?, query: MutableRootQuery<Entity<FakeID>, FakeID>) {
        filter?.let {
            it.execute(this, FilterExecutionContext(query, mutableSetOf()), argumentsConverter)
        }
    }

    suspend fun fetch(prop: MutationProp, env: DataFetchingEnvironment): Any? {
        val function = (prop as MutationPropImpl).function
        val javaMethod = function.javaMethod ?: error("Internal bug: No java method for '$function'")
        val owner = applicationContext.getBean(javaMethod.declaringClass)
        val args = argumentsConverter.convert(
            prop.arguments,
            owner,
            env
        )
        return function.callSuspend(*args)
    }
}