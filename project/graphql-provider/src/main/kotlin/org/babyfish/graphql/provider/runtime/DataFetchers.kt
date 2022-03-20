package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.future.await
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.meta.MetaProvider
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.MutationProp
import org.babyfish.graphql.provider.meta.QueryProp
import org.babyfish.graphql.provider.meta.impl.MutationPropImpl
import org.babyfish.graphql.provider.runtime.loader.BatchLoaderByParentId
import org.babyfish.graphql.provider.runtime.loader.ManyToManyBatchLoader
import org.babyfish.graphql.provider.runtime.loader.NonManyToManyBatchLoader
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.meta.config.Column
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.springframework.aop.support.AopUtils
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.javaMethod

open class DataFetchers(
    private val r2dbcClient: R2dbcClient,
    private val argumentsConverter: ArgumentsConverter,
    private val applicationContext: ApplicationContext
) {

    @Suppress("UNCHECKED_CAST")
    suspend fun fetch(prop: QueryProp, env: DataFetchingEnvironment): Any? =
        r2dbcClient.query(
            prop.targetType!!.kotlinType as KClass<Entity<FakeID>>,
        ) {
            prop.filter.execute(
                env,
                FilterExecutionContext(this, mutableSetOf()),
                argumentsConverter
            )
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
                BatchLoaderByParentId(r2dbcClient, prop),
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
                        NonManyToManyBatchLoader(r2dbcClient, prop)
                    else ->
                        ManyToManyBatchLoader(r2dbcClient, prop)
                },
                DataLoaderOptions().setMaxBatchSize(64)
            )
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