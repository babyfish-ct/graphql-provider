package org.babyfish.graphql.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.babyfish.graphql.provider.dsl.*
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.babyfish.graphql.provider.runtime.filterExecutionContext
import org.babyfish.graphql.provider.runtime.loader.UserImplementationLoader
import org.babyfish.graphql.provider.runtime.registerEntityFieldFilter
import org.babyfish.graphql.provider.runtime.registerEntityFieldImplementation
import org.babyfish.graphql.provider.runtime.userImplementationExecutionContext
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory
import org.dataloader.DataLoaderOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.GenericTypeResolver
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
@GraphQLProviderDSL
abstract class EntityMapper<E: Entity<ID>, ID: Comparable<ID>> {

    val immutableType: ImmutableType

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var cfg: GraphQLProviderProperties

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

    protected fun <T: Any> spring(type: KClass<T>): T =
        applicationContext.getBean(type.java)

    inner class Runtime internal constructor() {

        fun <X: Entity<XID>, XID: Comparable<XID>> filterList(
            prop: KProperty1<E, List<X>?>,
            block: FilterDSL<X, XID>.() -> Unit
        ) {
            if (!registerEntityFieldFilter(prop, this@EntityMapper)) {
                FilterDSL<X, XID>(filterExecutionContext).block()
            }
        }

        fun <X: Entity<XID>, XID: Comparable<XID>> filterConnection(
            prop: KProperty1<E, Connection<X>?>,
            block: FilterDSL<X, XID>.() -> Unit
        ) {
            if (!registerEntityFieldFilter(prop, this@EntityMapper)) {
                FilterDSL<X, XID>(filterExecutionContext).block()
            }
        }

        fun <T> implementation(
            prop: KProperty1<E, T>,
            block: suspend (E) -> T,
        ) {
            if (!registerEntityFieldImplementation(prop, this@EntityMapper)) {
                val ctx = userImplementationExecutionContext
                val row = ctx.env.getSource<E>()
                ctx.result = mono(Dispatchers.Unconfined) {
                    block(row)
                }.toFuture() as CompletableFuture<Any?>
            }
        }

        fun <T> batchImplementation(
            prop: KProperty1<E, T?>,
            block: suspend (ids: Set<ID>) -> Map<ID, T>
        ) {
            if (!registerEntityFieldImplementation(prop, this@EntityMapper)) {
                val ctx = userImplementationExecutionContext
                val row = ctx.env.getSource<E>()
                val dataLoader: DataLoader<Any, Any?> =
                    ctx.env.dataLoaderRegistry.computeIfAbsent(
                        "graphql-provider:loader-by-user-implementation:$prop"
                    ) {
                        DataLoaderFactory.newMappedDataLoader(
                            UserImplementationLoader(block as suspend (Set<Any>) -> Map<Any, Any?>),
                            DataLoaderOptions().setMaxBatchSize(cfg.batchSize(ctx.prop))
                        )
                    }
                ctx.result = dataLoader.load(row.id)
            }
        }
    }
}