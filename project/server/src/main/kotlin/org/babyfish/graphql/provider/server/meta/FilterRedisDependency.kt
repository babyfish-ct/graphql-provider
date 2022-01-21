package org.babyfish.graphql.provider.server.meta

interface FilterRedisDependency {
    val prop: EntityProp
    val dependencies: Collection<FilterRedisDependency>
}