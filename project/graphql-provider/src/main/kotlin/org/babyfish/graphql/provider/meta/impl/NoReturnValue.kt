package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.runtime.dynamicConfigurationRegistryFunScope
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction

internal fun invokeByRegistryMode(owner: Any, function: KFunction<*>) {
    dynamicConfigurationRegistryFunScope(function) {
        val args = Array<Any?>(function.parameters.size) { null }
        args[0] = owner
        for (index in it.indices) {
            args[index + 1] = it[index].defaultValue()
        }
        try {
            function.call(*args)
        } catch (ex: InvocationTargetException) {
            if (ex.targetException !is NoReturnValue) {
                throw ex
            }
        }
    }
}

internal class NoReturnValue: Exception(
    "The wrapper functions of Query.Runtime.queryReference, Query.Runtime.queryList and Query.queryConnection " +
        "cannot be invoked directly in your code " +
        "because they can only be invoked by the framework internally."
)