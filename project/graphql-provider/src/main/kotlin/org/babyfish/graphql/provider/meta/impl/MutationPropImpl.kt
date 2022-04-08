package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.graphql.provider.runtime.DynamicConfigurationRegistry
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class MutationPropImpl(
    function: KFunction<*>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>,
    dynamicConfigurationRegistry: DynamicConfigurationRegistry
) : RootPropImpl(function, modelTypeMap), MutationProp {

    override val userImplementation: UserImplementation? =
        dynamicConfigurationRegistry.userImplementation(function)

    override val arguments: List<Argument> = Argument.of(function)

    override fun toString(): String =
        function.toString()

    override val securityPredicate: SecurityPredicate?
        get() = TODO("Not yet implemented")
}