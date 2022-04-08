package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.Mutation
import org.babyfish.graphql.provider.Query
import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.meta.impl.FilterImpl
import org.babyfish.graphql.provider.meta.impl.UserImplementationImpl
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

internal class DynamicConfigurationRegistry {

    private val filterMap = mutableMapOf<String, FilterImpl>()

    private val userImplementationMap = mutableMapOf<String, UserImplementationImpl>()

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

    fun addImplementation(
        query: Query,
        fn: KFunction<*>,
        fnArguments: List<Argument>
    ) {
        val path = "Query.${fn.name}"
        userImplementationMap[path]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '$registryFun'")
        }
        userImplementationMap[path] = UserImplementationImpl(
            query,
            fn,
            fnArguments
        )
    }

    fun addImplementation(
        mutation: Mutation,
        fn: KFunction<*>,
        fnArguments: List<Argument>
    ) {
        val path = "Mutation.${fn.name}"
        userImplementationMap[path]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '$registryFun'")
        }
        userImplementationMap[path] = UserImplementationImpl(
            mutation,
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
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '$registryFun'")
        }
        filterMap[path] = FilterImpl(
            mapper,
            fn,
            fnArguments
        )
    }

    fun addImplementation(
        prop: KProperty1<out Entity<*>, *>,
        mapper: org.babyfish.graphql.provider.EntityMapper<out Entity<*>, *>,
        fn: KFunction<*>,
        fnArguments: List<Argument>
    ) {
        val path = prop.path
        userImplementationMap[path]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '$registryFun'")
        }
        userImplementationMap[path] = UserImplementationImpl(
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

    fun filter(function: KFunction<*>): Filter? =
        filterMap["Query.${function.name}"]

    fun userImplementation(function: KFunction<*>): UserImplementation? =
        (function.parameters[0].type.classifier as? KClass<*>)?.let {
            when {
                Query::class.java.isAssignableFrom(it.java) ->
                    userImplementationMap["Query.${function.name}"]
                Mutation::class.java.isAssignableFrom(it.java) ->
                    userImplementationMap["Mutation.${function.name}"]
                else -> error("Internal bug: '$function' is declared in neither Query nor Mutation")
            }
        }

    fun filter(prop: KProperty1<*, *>): Filter? =
        filterMap[prop.path]

    fun userImplementation(prop: KProperty1<*, *>): UserImplementation? =
        userImplementationMap[prop.path]

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

internal fun registerQueryFieldFilter(
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

internal fun registerQueryFieldImplementation(
    query: Query
): Boolean {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        return false
    }
    reg.addImplementation(query, fn, registryFunArguments ?: error("Internal bug"))
    return true
}

internal fun registerMutationFieldImplementation(
    mutation: Mutation
): Boolean {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        return false
    }
    reg.addImplementation(mutation, fn, registryFunArguments ?: error("Internal bug"))
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

internal fun registerEntityFieldFilter(
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

internal fun registerEntityFieldImplementation(
    prop: KProperty1<out Entity<*>, *>,
    mapper: org.babyfish.graphql.provider.EntityMapper<out Entity<*>, *>
): Boolean {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        return false
    }
    reg.addImplementation(
        prop,
        mapper,
        fn,
        registryFunArguments ?: error("Internal bug")
    )
    return true
}

private val KProperty1<*, *>.path: String
    get() {
        val getter = javaGetter ?: throw IllegalArgumentException("Illegal prop '$this', no java getter")
        return "${getter.declaringClass.name}.${name}"
    }

private val DEFAULT_QUERY_FIELD_CACHE = Cache(CacheLevel.NO_CACHE)
