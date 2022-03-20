package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.meta.Cache
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.CacheLevel
import org.babyfish.graphql.provider.meta.Filter
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.kimmer.sql.Entity
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

internal class DynamicConfigurationRegistry {

    private val filterMap = mutableMapOf<String, FilterImpl>()

    private val cacheMap = mutableMapOf<String, Cache>()

    fun addFilter(
        query: Query,
        fn: KFunction<*>,
        fnArguments: List<Argument>
    ) {
        val path = "Query.${fn.name}"
        filterMap[path]?.let {
            throw ModelException("Conflict query function: '${it.fn}' and '${registryFun}'")
        }
        filterMap[path] = FilterImpl(
            query,
            fn,
            fnArguments
        )
    }

    fun addFilter(
        prop: KProperty1<out Entity<*>, *>,
        mapper: org.babyfish.graphql.provider.EntityMapper<out Entity<*>, *>,
        fn: KFunction<*>,
        fnArguments: List<Argument>
    ) {
        val path = prop.path
        filterMap[path]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '${registryFun}'")
        }
        filterMap[path] = FilterImpl(
            mapper,
            fn,
            fnArguments
        )
    }

    fun addQueryFieldCache(
        fn: KFunction<*>,
        cache: Cache
    ) {
        val path = "Query.${fn.name}"
        cacheMap[path] = cache
    }

    fun filter(function: KFunction<*>): Filter =
        filterMap["Query.${function.name}"]
            ?: throw IllegalArgumentException("The function '$function' is not query method")

    fun filter(prop: KProperty1<*, *>): Filter? =
        filterMap[prop.path]

    fun cache(fn: KFunction<*>): Cache =
        cacheMap["Query.${fn.name}"] ?: DEFAULT_QUERY_FIELD_CACHE
}

private var registry: DynamicConfigurationRegistry? = null

private var registryFun: KFunction<*>? = null

private var registryFunArguments: List<Argument>? = null

internal fun dynamicConfigurationRegistryScope(
    dynamicConfigurationRegistry: DynamicConfigurationRegistry,
    block: () -> Unit
) {
    registry = dynamicConfigurationRegistry
    try {
        block()
    } finally {
        registry = null
    }
}

internal fun dynamicConfigurationRegistryFunScope(fn: KFunction<*>, block: (List<Argument>) -> Unit) {
    val args = Argument.of(fn)
    registryFunArguments = args
    registryFun = fn
    try {
        block(args)
    } finally {
        registryFun = null
        registryFunArguments = null
    }
}

private class FilterImpl(
    val fnOwner: Any,
    val fn: KFunction<*>,
    override val arguments: List<Argument>
): Filter {

    override fun execute(
        env: DataFetchingEnvironment,
        ctx: FilterExecutionContext,
        argumentsConverter: ArgumentsConverter
    ) {
        val args = argumentsConverter.convert(
            arguments,
            fnOwner,
            env
        )
        val oldContext = filterExecutionContextLocal.get()
        filterExecutionContextLocal.set(ctx)
        try {
            fn.call(*args)
        } catch (ex: InvocationTargetException) {
            if (ex.targetException !is NoReturnValue) {
                throw ex.targetException
            }
        } finally {
            if (oldContext !== null) {
                filterExecutionContextLocal.set(oldContext)
            } else {
                filterExecutionContextLocal.remove()
            }
        }
    }
}

internal fun registerQueryField(
    query: Query
): Boolean {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        return false
    }
    reg.addFilter(query, fn, registryFunArguments ?: error("Internal bug"))
    return true
}

internal fun registerEntityField(
    prop: KProperty1<out Entity<*>, *>,
    mapper: org.babyfish.graphql.provider.EntityMapper<out Entity<*>, *>
): Boolean {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        return false
    }
    reg.addFilter(prop, mapper, fn, registryFunArguments ?: error("Internal bug"))
    return true
}

internal fun registerQueryFieldCache(cache: Cache) {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        error("Internal bug: Cannot call 'registerQueryFieldRedis'")
    }
    reg.addQueryFieldCache(fn, cache)
}

internal val filterExecutionContext
    get() = filterExecutionContextLocal.get() ?: error(
        "No FilterExecutionContext. wrapper functions of " +
            "Query.queryReference, Query.queryList, Query.queryConnection, " +
            "EntityMapper.filterList, EntityMapper.filterConnection, and EntityMapper.userImplementation " +
            "cannot be invoked directly because they can only be invoked by the framework internally"
    )

private val filterExecutionContextLocal = ThreadLocal<FilterExecutionContext>()

private val KProperty1<*, *>.path: String
    get() {
        val getter = javaGetter ?: throw IllegalArgumentException("Illegal prop '$this', no java getter")
        return "${getter.declaringClass.name}.${name}"
    }

private val DEFAULT_QUERY_FIELD_CACHE = Cache(CacheLevel.NO_CACHE)
