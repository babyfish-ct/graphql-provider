package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.Filter
import org.babyfish.graphql.provider.meta.QueryProp
import org.babyfish.graphql.provider.meta.Cache
import org.babyfish.graphql.provider.runtime.GraphQLTypeGenerator
import kotlin.reflect.KFunction

internal class QueryPropImpl(
    generator: GraphQLTypeGenerator,
    val function: KFunction<*>
): AbstractRootPropImpl(generator, function), QueryProp {

    override val filter: Filter =
        generator.dynamicConfigurationRegistry.filter(function)

    override val cache: Cache =
        generator.dynamicConfigurationRegistry.cache(function)
}

