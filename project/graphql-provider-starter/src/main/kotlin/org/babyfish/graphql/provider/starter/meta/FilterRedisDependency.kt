package org.babyfish.graphql.provider.starter.meta

interface FilterRedisDependency {
    val prop: EntityProp
    val dependencies: Collection<FilterRedisDependency>
}