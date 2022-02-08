package org.babyfish.graphql.provider.starter.meta.impl

import org.babyfish.graphql.provider.starter.meta.Filter
import org.babyfish.graphql.provider.starter.meta.QueryProp
import org.babyfish.graphql.provider.starter.meta.Redis
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import kotlin.reflect.KFunction

internal class QueryPropImpl(
    generator: GraphQLTypeGenerator,
    val function: KFunction<*>
): AbstractRootPropImpl(generator, function), QueryProp {

    override val filter: Filter =
        generator.dynamicConfigurationRegistry.filter(function)

    override val redis: Redis =
        generator.dynamicConfigurationRegistry.redis(function)
}

