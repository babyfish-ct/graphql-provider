package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.ModelType
import org.babyfish.graphql.provider.meta.MutationProp
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class MutationPropImpl(
    function: KFunction<*>,
    modelTypeMap: Map<KClass<out Entity<*>>, ModelType>
) : RootPropImpl(function, modelTypeMap), MutationProp {

    override val arguments: List<Argument> = Argument.of(function)
}