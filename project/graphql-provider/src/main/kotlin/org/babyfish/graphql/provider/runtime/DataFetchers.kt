package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.meta.impl.MutationPropImpl
import org.babyfish.graphql.provider.meta.impl.QueryPropImpl
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.runtime.loader.BatchLoaderByParentId
import org.babyfish.graphql.provider.runtime.loader.ManyToManyBatchLoader
import org.babyfish.graphql.provider.runtime.loader.NonManyToManyBatchLoader
import org.babyfish.graphql.provider.security.AuthenticationExtractor
import org.babyfish.graphql.provider.security.SecurityChecker
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.*
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.ast.query.MutableRootQuery
import org.babyfish.kimmer.sql.meta.config.Column
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.springframework.context.ApplicationContext
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.javaMethod

open class DataFetchers(
    private val applicationContext: ApplicationContext,
    private val r2dbcClient: R2dbcClient,
    private val argumentsConverter: ArgumentsConverter,
    private val properties: GraphQLProviderProperties,
    private val authenticationExtractor: AuthenticationExtractor?,
    private val securityChecker: SecurityChecker,
    private val executor: Executor
) {

    fun fetch(
        prop: QueryProp,
        env: DataFetchingEnvironment
    ): CompletableFuture<Any?> =
        fetchBySuspendFun(prop, env)

    fun fetch(
        prop: MutationProp,
        env: DataFetchingEnvironment
    ): CompletableFuture<Any?> =
        fetchBySuspendFun(prop, env)

    private fun fetchBySuspendFun(
        prop: GraphQLProp,
        env: DataFetchingEnvironment
    ): CompletableFuture<Any?> {
        val function = if (prop is QueryProp) {
            (prop as QueryPropImpl).function
        } else {
            (prop as MutationPropImpl).function
        }
        val javaMethod = function.javaMethod ?: error("Internal bug: No java method for '$function'")
        val owner = applicationContext.getBean(javaMethod.declaringClass)
        val authentication = authenticationExtractor?.get(env)
        securityChecker.check(
            authentication,
            when (owner) {
                is Query -> owner.securityPredicate()
                is Mutation -> owner.securityPredicate()
                else -> null
            }
        )
        val args = argumentsConverter.convert(
            prop.arguments,
            owner,
            env
        )
        return graphqlMono(ExecutorContext(prop, env, authentication)) {
            try {
                function.callSuspendBy(args)
            } catch (ex: InvocationTargetException) {
                throw ex.targetException
            }
        }.toFuture()
    }

    fun fetch(
        prop: ModelProp,
        env: DataFetchingEnvironment
    ): Any? {
        securityChecker.check(
            authenticationExtractor?.get(env),
            prop.securityPredicate,
            prop.declaringType.securityPredicate
        )
        return fetchUserImplementation(prop, env)
            ?: if (prop.isAssociation) {
                fetchAssociation(prop, env)
            } else {
                val entity = env.getSource<Entity<*>>()
                return CompletableFuture.completedFuture(
                    Immutable.get(entity, prop.immutableProp)
                )
            }
    }

    private fun fetchUserImplementation(
        prop: ModelProp,
        env: DataFetchingEnvironment
    ): CompletableFuture<Any?>? {
        val userImplementation = prop.userImplementation ?: return null
        return userImplementation.execute(
            UserImplementationExecutionContext(
                prop,
                env,
                argumentsConverter,
                authenticationExtractor?.get(env)
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun fetchAssociation(
        prop: ModelProp,
        env: DataFetchingEnvironment
    ): CompletableFuture<Any?> {
        if (prop.isConnection) {
            return graphqlMono(prop, env, authenticationExtractor?.get(env)) {
                fetchConnectionAsync(prop, env)
            }.toFuture() as CompletableFuture<Any?>
        }
        val entity = env.getSource<Entity<*>>()
        val idOnly = env.selectionSet.fields.let {
            it.size == 1 && it[0].name == "id"
        }
        if (prop.isReference && prop.storage is Column && Immutable.isLoaded(entity, prop.immutableProp)) {
            val parent = Immutable.get(entity, prop.immutableProp) as Entity<*>?
            if (parent === null) {
                return CompletableFuture.completedFuture(null)
            }
            val parentId = Immutable.get(parent, prop.targetType!!.idProp.immutableProp)
            if (env.arguments.isEmpty()) {
                if (idOnly) {
                    return CompletableFuture.completedFuture(
                            produce(prop.targetType!!.kotlinType) {
                            Draft.set(this, prop.targetType!!.idProp.immutableProp, parentId)
                        }
                    )
                }
            }
            return env.loaderByParentId(prop).load(parentId)
        } else {
            val future = env.loaderById(prop, idOnly).load(entity.id)
            if (prop.isReference) {
                return future.thenApply { it.firstOrNull() }
            }
            return future.thenApply { it ?: emptyList<Any>() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun fetchConnectionAsync(
        prop: ModelProp,
        env: DataFetchingEnvironment
    ): Connection<*> =
        executor.queryConnection<Entity<FakeID>, FakeID>(prop.filter!!.raw)

    private fun DataFetchingEnvironment.loaderByParentId(prop: ModelProp): DataLoader<Any, Any?> {
        val dataLoaderKey = "graphql-provider:loader-by-parent-id:${prop}"
        return dataLoaderRegistry.computeIfAbsent(dataLoaderKey) {
            DataLoaderFactory.newMappedDataLoader(
                BatchLoaderByParentId(r2dbcClient, prop, authenticationExtractor?.get(this)) {
                    applyFilter(prop, it)
                },
                DataLoaderOptions().setMaxBatchSize(properties.batchSize(prop))
            )
        }
    }

    private fun DataFetchingEnvironment.loaderById(prop: ModelProp, idOnly: Boolean): DataLoader<Any, List<Any>> {
        val dataLoaderKey = "graphql-provider:loader-by-id:${prop}"
        return dataLoaderRegistry.computeIfAbsent(dataLoaderKey) {
            DataLoaderFactory.newMappedDataLoader(
                when {
                    prop.isReference || prop.opposite?.isReference == true ->
                        NonManyToManyBatchLoader(r2dbcClient, prop, authenticationExtractor?.get(this)) {
                            applyFilter(prop, it)
                        }
                    else ->
                        ManyToManyBatchLoader(r2dbcClient, prop, idOnly, authenticationExtractor?.get(this)) {
                            applyFilter(prop, it)
                        }
                },
                DataLoaderOptions().setMaxBatchSize(properties.batchSize(prop))
            )
        }
    }

    private fun DataFetchingEnvironment.applyFilter(prop: ModelProp, query: MutableRootQuery<Entity<FakeID>, FakeID>) {
        prop.filter?.let {
            it.apply(FilterExecutionContext(prop, this, argumentsConverter, query))
        }
    }
}