package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.*
import org.babyfish.graphql.provider.dsl.runtime.PropBatchLoadingCodeDSL
import org.babyfish.graphql.provider.dsl.runtime.PropLoadingCodeDSL
import org.babyfish.graphql.provider.meta.ModelProp
import org.babyfish.graphql.provider.meta.execute
import org.babyfish.graphql.provider.runtime.*
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.runtime.filterExecutionContext
import org.babyfish.graphql.provider.runtime.loader.UserImplementationLoader
import org.babyfish.graphql.provider.security.SecurityChecker
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import org.springframework.security.core.Authentication
import org.springframework.transaction.ReactiveTransactionManager
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@GraphQLProviderDSL
abstract class EntityMapper<E: Entity<ID>, ID: Comparable<ID>> {

    val immutableType: ImmutableType

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var properties: GraphQLProviderProperties

    private var tmpRegistry: TmpRegistry? = null

    init {
        val arguments =
            GenericTypeResolver
                .resolveTypeArguments(this::class.java,  EntityMapper::class.java)
                ?: throw ModelException(
                    "Illegal class '${this::class.qualifiedName}', " +
                        "type argument of '${ EntityMapper::class.qualifiedName}' is not specified"
                )
        val entityJavaType = arguments[0]
        if (entityJavaType.simpleName == "Query") {
            throw ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${ EntityMapper::class.qualifiedName}' " +
                    "cannot be specified as 'Query'"
            )
        }
        if (!entityJavaType.isInterface && !Entity::class.java.isAssignableFrom(entityJavaType)) {
            throw ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${ EntityMapper::class.qualifiedName}' " +
                    "must be specified as derived interface of '${Entity::class.qualifiedName}'"
            )
        }
        if (Connection::class.java.isAssignableFrom(entityJavaType)) {
            throw ModelException(
                "Illegal class'${this::class.qualifiedName}', " +
                    "type argument of '${ EntityMapper::class.qualifiedName}' " +
                    "cannot be specified as derived interface of '${Connection::class.qualifiedName}'"
            )
        }
        immutableType = ImmutableType.of(entityJavaType as Class<out Entity<*>>)
    }

    abstract fun EntityTypeDSL<E, ID>.config()

    protected val runtime: Runtime = Runtime()

    protected fun <T:Any> spring(beanType: KClass<T>): T =
        applicationContext.getBean(beanType.java)

    internal fun register(
        dynamicConfigurationRegistry: DynamicConfigurationRegistry,
        proxy: EntityMapper<*, *>, // this of proxy of this
        fn: KFunction<*>,
        block: () -> Unit
    ) {
        tmpRegistry = TmpRegistry(dynamicConfigurationRegistry, proxy, fn)
        try {
            block()
        } finally {
            tmpRegistry = null
        }
    }

    inner class Runtime internal constructor() {

        fun <X: Entity<XID>, XID: Comparable<XID>> filterList(
            prop: KProperty1<E, List<X>?>,
            block: FilterDSL<X, XID>.(Authentication?) -> Unit
        ) {
            filter(prop, block as FilterDSL<*, *>.(Authentication?) -> Unit)
        }

        fun <X: Entity<XID>, XID: Comparable<XID>> filterConnection(
            prop: KProperty1<E, Connection<X>?>,
            block: FilterDSL<X, XID>.(Authentication?) -> Unit
        ) {
            filter(prop, block as FilterDSL<*, *>.(Authentication?) -> Unit)
        }

        private fun filter(
            prop: KProperty1<out Entity<*>, *>,
            block: FilterDSL<*, *>.(Authentication?) -> Unit
        ) {
            val registry = tmpRegistry
            if (registry !== null) {
                registry.apply {
                    dynamicConfigurationRegistry.addFilter(prop, proxy, fn, block)
                }
            } else {
                val ctx = filterExecutionContext()
                val dsl = FilterDSL(ctx.filterable)
                dsl.block(ctx.authentication)
                ctx.securityPredicate = dsl.predicate()
            }
        }

        fun <T> implement(
            prop: KProperty1<E, T>,
            block: suspend (E) -> T
        ) {
            implementBy(prop) {
                async {
                    block(it)
                }
            }
        }

        fun <T> implementBy(
            prop: KProperty1<E, T>,
            block: PropLoadingCodeDSL<E, ID, T>.() -> Unit,
        ) {
            val registry = tmpRegistry
            if (registry !== null) {
                registry.apply {
                    dynamicConfigurationRegistry.addUserImplementation(prop, proxy, fn)
                }
            } else {
                val ctx = userImplementationExecutionContext()
                val row = ctx.env.getSource<E>()
                val dsl = PropLoadingCodeDSL<E, ID, T>()
                dsl.block()
                spring(SecurityChecker::class).check(ctx.authentication, dsl.predicate())
                ctx.result = graphqlMono(ExecutorContext(ctx.prop, ctx.env, ctx.authentication)) {
                    spring(ReactiveTransactionManager::class).execute(dsl.transaction()) {
                        dsl.async()(row)
                    }
                }.toFuture() as CompletableFuture<Any?>
            }
        }

        fun <T: Any> batchImplement(
            prop: KProperty1<E, T?>,
            block: suspend (Set<ID>) -> Map<ID, T>
        ) {
            batchImplementBy(prop) {
                async {
                    block(it)
                }
            }
        }

        fun <T: Any> batchImplementBy(
            prop: KProperty1<E, T?>,
            block: PropBatchLoadingCodeDSL<ID, T>.() -> Unit
        ) {
            val registry = tmpRegistry
            if (registry !== null) {
                registry.apply {
                    dynamicConfigurationRegistry.addUserImplementation(prop, proxy, fn)
                }
            } else {
                val ctx = userImplementationExecutionContext()
                val row = ctx.env.getSource<E>()
                val dsl = PropBatchLoadingCodeDSL<ID, T>()
                dsl.block()
                spring(SecurityChecker::class).check(ctx.authentication, dsl.predicate())
                val loaderFun: suspend (Set<Any>) -> Map<Any, Any> = {
                    spring(ReactiveTransactionManager::class).execute(dsl.transaction()) {
                        (dsl.async() as suspend (Set<Any>) -> Map<Any, Any>)(it)
                    }
                }
                val dataLoader: DataLoader<Any, Any?> =
                    ctx.env.dataLoaderRegistry.computeIfAbsent(
                        "graphql-provider:loader-by-user-implementation:$prop"
                    ) {
                        DataLoaderFactory.newMappedDataLoader(
                            UserImplementationLoader(
                                ctx.authentication,
                                loaderFun
                            ),
                            DataLoaderOptions().setMaxBatchSize(properties.batchSize(ctx.prop as ModelProp))
                        )
                    }
                ctx.result = dataLoader.load(row.id)
            }
        }
    }

    private class TmpRegistry(
        val dynamicConfigurationRegistry: DynamicConfigurationRegistry,
        val proxy: EntityMapper<*, *>,
        val fn: KFunction<*>
    )
}