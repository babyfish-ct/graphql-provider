package org.babyfish.graphql.provider.starter.meta

interface QueryProp : GraphQLProp {
    val filter: Filter
    val redis: Redis
    override val arguments: List<Argument>
        get() = filter.arguments
}