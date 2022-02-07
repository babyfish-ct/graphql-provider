package org.babyfish.graphql.provider.starter.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.starter.EntityMapper
import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.meta.Argument
import org.babyfish.graphql.provider.starter.meta.Filter
import org.babyfish.kimmer.Immutable
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1

internal class FilterRegistry {

    private val map = mutableMapOf<KCallable<*>, FilterImpl>()

    fun add(
        prop: KProperty1<out Immutable, *>,
        mapper: EntityMapper<out Immutable>,
        fn: KFunction<*>
    ) {
        map[prop]?.let {
            throw ModelException("Conflict entity mapper function: '${it.fn}' and '${registryFun}'")
        }
        map[prop] = FilterImpl(
            mapper,
            fn
        )
    }
}

private var registry: FilterRegistry? = null

private var registryFun: KFunction<*>? = null

internal fun filterRegistryScope(block: () -> Unit) {
    registry = FilterRegistry()
    try {
        block()
    } finally {
        registry = null
    }
}

internal fun filterRegistryFunScope(fn: KFunction<*>, block: () -> Unit) {
    registryFun = fn
    try {
        block()
    } finally {
        registryFun = null
    }
}

private class FilterImpl(
    val fnOwner: Any,
    val fn: KFunction<*>
): Filter {

    override val arguments: List<Argument> = Argument.of(fn)

    override fun execute(
        env: DataFetchingEnvironment,
        ctx: FilterExecutionContext
    ) {
        val args = Array<Any?>(fn.parameters.size) { null }
        args[0] = fnOwner
        for (index in 1 until fn.parameters.size) {
            args[index] = env.getArgument(fn.parameters[index].name)
        }
        val oldContext = filterExecutionContextLocal.get()
        filterExecutionContextLocal.set(ctx)
        try {
            fn.call(*args)
        } finally {
            if (oldContext !== null) {
                filterExecutionContextLocal.set(oldContext)
            } else {
                filterExecutionContextLocal.remove()
            }
        }
    }
}

internal fun registryEntityField(
    prop: KProperty1<out Immutable, *>,
    mapper: EntityMapper<out Immutable>
): Boolean {
    val reg = registry
    val fn = registryFun
    if (reg === null || fn === null) {
        return false
    }
    reg.add(prop, mapper, fn)
    return true
}

internal val filterExecutionContext
    get() = filterExecutionContextLocal.get() ?: error("No FilterExecutionContext")

private val filterExecutionContextLocal = ThreadLocal<FilterExecutionContext>()


