package org.babyfish.graphql.provider.meta

interface QueryProp : GraphQLProp {
    val filter: Filter
    val cache: Cache
    override val arguments: List<Argument>
        get() = filter.arguments
}